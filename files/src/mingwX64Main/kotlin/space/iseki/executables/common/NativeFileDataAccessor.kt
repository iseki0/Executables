package space.iseki.executables.common

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.plus
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.posix.memcpy
import platform.windows.CloseHandle
import platform.windows.CreateFileMappingW
import platform.windows.CreateFileW
import platform.windows.DWORD
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_PATH_NOT_FOUND
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.FILE_FLAG_RANDOM_ACCESS
import platform.windows.FILE_MAP_READ
import platform.windows.FILE_SHARE_DELETE
import platform.windows.FILE_SHARE_READ
import platform.windows.FILE_SHARE_WRITE
import platform.windows.FORMAT_MESSAGE_ALLOCATE_BUFFER
import platform.windows.FORMAT_MESSAGE_FROM_SYSTEM
import platform.windows.FORMAT_MESSAGE_IGNORE_INSERTS
import platform.windows.FormatMessageW
import platform.windows.GENERIC_READ
import platform.windows.GetFileSizeEx
import platform.windows.GetLastError
import platform.windows.HANDLE
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.LANG_NEUTRAL
import platform.windows.LARGE_INTEGER
import platform.windows.LPVOID
import platform.windows.LPWSTRVar
import platform.windows.LocalFree
import platform.windows.MapViewOfFile
import platform.windows.OPEN_EXISTING
import platform.windows.PAGE_READONLY
import platform.windows.SUBLANG_DEFAULT
import platform.windows.UnmapViewOfFile
import space.iseki.executables.share.ClosableDataAccessor
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.Cleaner
import kotlin.native.ref.createCleaner
import kotlin.native.terminateWithUnhandledException

private fun makeLangId(primary: Int, sub: Int) = (sub shl 10) or primary

@OptIn(ExperimentalForeignApi::class)
private fun formatMessage(errorCode: DWORD, context: String = ""): String {
    memScoped {
        val buffer = alloc<LPWSTRVar>()
        val size = FormatMessageW(
            (FORMAT_MESSAGE_ALLOCATE_BUFFER or FORMAT_MESSAGE_FROM_SYSTEM or FORMAT_MESSAGE_IGNORE_INSERTS).toUInt(),
            null,
            errorCode,
            makeLangId(LANG_NEUTRAL, SUBLANG_DEFAULT).toUInt(),
            buffer.ptr.reinterpret(),
            0u,
            null,
        )

        if (size == 0u) return "Unknown error"
        val message = buffer.value?.toKStringFromUtf16().orEmpty()
        LocalFree(buffer.value)
        return message.trim()
    }
}

/**
 * Creates a Windows system call exception for critical failures without throwing.
 * This should be used when a Windows API call fails and indicates a serious system error.
 */
@OptIn(ExperimentalForeignApi::class)
private fun createCriticalSystemCallException(systemCall: String, errorCode: DWORD? = null): IOException {
    val actualErrorCode = errorCode ?: GetLastError()
    val errorMessage = formatMessage(actualErrorCode, systemCall)
    return IOException("Critical Windows system call failed: $systemCall (error code: $actualErrorCode) - $errorMessage")
}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
internal class NativeFileDataAccessor(private val nativePath: String) : ClosableDataAccessor() {
    private val beginPtr: LPVOID?
    override val size: Long
    private val unmapHolder: UnmapHolder?
    private val cleaner: Cleaner?

    init {
        var fileMappingHandle: HANDLE? = null
        var beginPtr: LPVOID? = null
        val fileHandle: HANDLE? = CreateFileW(
            lpFileName = nativePath,
            dwDesiredAccess = GENERIC_READ,
            dwShareMode = (FILE_SHARE_READ or FILE_SHARE_WRITE or FILE_SHARE_DELETE).toUInt(),
            lpSecurityAttributes = null,
            dwCreationDisposition = OPEN_EXISTING.toUInt(),
            dwFlagsAndAttributes = (FILE_ATTRIBUTE_NORMAL or FILE_FLAG_RANDOM_ACCESS).toUInt(),
            hTemplateFile = null,
        )
        if (fileHandle == INVALID_HANDLE_VALUE) {
            throw translateErrorImmediately("CreateFileW")
        }
        fileHandle!!
        var th: Throwable? = null
        var size: Long = 0
        try {
            size = getFileSize(fileHandle)
            if (size > 0L) {
                fileMappingHandle = CreateFileMappingW(
                    hFile = fileHandle,
                    lpFileMappingAttributes = null,
                    flProtect = PAGE_READONLY.toUInt(),
                    dwMaximumSizeHigh = 0u,
                    dwMaximumSizeLow = 0u,
                    lpName = null,
                )
                if (fileMappingHandle == null) {
                    throw translateErrorImmediately("CreateFileMappingW")
                }
                beginPtr = MapViewOfFile(
                    hFileMappingObject = fileMappingHandle,
                    dwDesiredAccess = FILE_MAP_READ.toUInt(),
                    dwFileOffsetHigh = 0u,
                    dwFileOffsetLow = 0u,
                    dwNumberOfBytesToMap = 0u,
                )
                if (beginPtr == null) {
                    throw translateErrorImmediately("MapViewOfFile")
                }
            }
        } catch (th0: Throwable) {
            th = th0
        }
        if (fileMappingHandle != null) {
            if (CloseHandle(fileMappingHandle) == 0) {
                // Critical: CloseHandle failure during cleanup should terminate the program
                // as it indicates a serious system state corruption
                terminateWithUnhandledException(createCriticalSystemCallException("CloseHandle(fileMappingHandle)"))
            }
        }
        if (CloseHandle(fileHandle) == 0) {
            // Critical: CloseHandle failure during cleanup should terminate the program
            // as it indicates a serious system state corruption
            terminateWithUnhandledException(createCriticalSystemCallException("CloseHandle(fileHandle)"))
        }
        th?.let { throw it }
        this.beginPtr = beginPtr
        if (beginPtr != null) {
            this.unmapHolder = UnmapHolder(beginPtr)
            this.cleaner = createCleaner(this.unmapHolder, UnmapHolder::close)
        } else {
            this.unmapHolder = null
            this.cleaner = null
        }
        this.size = size
    }

    private fun getFileSize(handle: HANDLE): Long {
        memScoped {
            val size = alloc<LARGE_INTEGER>()
            if (GetFileSizeEx(handle, size.ptr) == 0) {
                throw translateErrorImmediately("GetFileSizeEx")
            }
            return size.QuadPart
        }
    }

    private fun translateErrorImmediately(nativeCall: String): IOException {
        val errorCode = GetLastError()
        val m = formatMessage(errorCode, nativeCall)
        val b = listOfNotNull(
            if (nativeCall.isNotEmpty()) "nativeCall: $nativeCall" else null,
            "errno: $errorCode",
            if (m.isNotEmpty()) "os_message: $m" else null,
        ).joinToString(", ")
        return when (errorCode.toInt()) {
            ERROR_FILE_NOT_FOUND, ERROR_PATH_NOT_FOUND -> NoSuchFileException(nativePath, null, b)
            ERROR_ACCESS_DENIED, 740 /* ERROR_ELEVATION_REQUIRED */ -> AccessDeniedException(nativePath, null, b)
            else -> IOException("Cannot open file $nativePath, $b")
        }
    }

    override fun doClose() {
        unmapHolder?.close()
    }

    override fun toString(): String = "NativeFileDataAccessor[MingW64](path=$nativePath)"

    override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
        DataAccessor.checkReadBounds(pos, buf, off, len)

        wrapRead {
            if (beginPtr == null) {
                return 0
            }
            val available = (size - pos).coerceAtLeast(0).coerceAtMost(len.toLong()).toInt()
            if (available == 0) return 0

            buf.usePinned { pinned ->
                val src = beginPtr.reinterpret<ByteVar>().plus(pos)
                val dst = pinned.addressOf(off)
                memcpy(dst, src, available.toULong())
            }

            return available
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal class UnmapHolder(private val ptr: LPVOID) : AutoCloseable {
    companion object {
        internal var nativeAccessCounter: AtomicInt? = null
    }

    init {
        nativeAccessCounter?.incrementAndGet()
    }

    private val closed = atomic(false)
    override fun close() {
        if (closed.compareAndSet(expect = false, update = true)) {
            nativeAccessCounter?.decrementAndGet()
            // Critical: UnmapViewOfFile failure indicates serious memory management corruption
            // This should never happen in normal operation and indicates a critical system error
            if (UnmapViewOfFile(ptr) == 0) {
                terminateWithUnhandledException(createCriticalSystemCallException("UnmapViewOfFile"))
            }
        }
    }
}
