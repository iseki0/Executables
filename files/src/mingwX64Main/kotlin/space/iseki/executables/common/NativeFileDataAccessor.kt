package space.iseki.executables.common

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
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

internal value class CloseFlag private constructor(private val s: ULong) {
    companion object {
        private val LMASK = 0x7fffffffffffffffuL
        private val HMASK = 0x8000000000000000uL
    }

    constructor() : this(0u)

    val closed: Boolean
        get() = s and HMASK != 0uL

    val shouldDoClose: Boolean
        get() = closed && s and LMASK == 0uL

    fun acquire(): CloseFlag {
        check(!closed)
        check(s < LMASK)
        return CloseFlag(s + 1u)
    }

    fun release(): CloseFlag {
        check(s and LMASK > 0uL)
        return CloseFlag(s - 1u)
    }

    fun close(): CloseFlag {
        check(!closed)
        return CloseFlag(s or HMASK)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal class NativeFileDataAccessor(private val nativePath: String) : DataAccessor {
    private val flag = atomic(CloseFlag())
    private val beginPtr: LPVOID?
    override val size: Long

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
                translateErrorImmediately("CloseHandle(fileMappingHandle)").also {
                    th?.addSuppressed(it) ?: run { th = it }
                }
            }
        }
        if (CloseHandle(fileHandle) == 0) {
            translateErrorImmediately("CloseHandle(fileHandle)").also {
                th?.addSuppressed(it) ?: run { th = it }
            }
        }
        th?.let { throw it }
        this.beginPtr = beginPtr
        this.size = size
    }


    private fun makeLangId(primary: Int, sub: Int) = (sub shl 10) or primary
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
        val m = formatMessage(errorCode)
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

    private fun formatMessage(errorCode: DWORD): String {
        memScoped {
            val buffer = alloc<LPWSTRVar>()
            val size = FormatMessageW(
                (FORMAT_MESSAGE_ALLOCATE_BUFFER or FORMAT_MESSAGE_FROM_SYSTEM or FORMAT_MESSAGE_IGNORE_INSERTS).toUInt(),
                null,
                errorCode,
                makeLangId(LANG_NEUTRAL, SUBLANG_DEFAULT).toUInt(),
                buffer.ptr.reinterpret(),
                0u,
                null
            )

            if (size == 0u) return ""
            val message = buffer.value?.toKStringFromUtf16().orEmpty()
            LocalFree(buffer.value)
            return message.trim()
        }
    }

    private fun unmap() {
        if (UnmapViewOfFile(beginPtr) == 0) {
            throw Error("native", translateErrorImmediately("UnmapViewOfFile"))
        }
    }

    override fun toString(): String = "NativeFileDataAccessor[MingW64](path=$nativePath)"
    override fun close() {
        flag.getAndUpdate {
            if (it.closed) {
                throw IOException("already closed")
            }
            it.close()
        }.also {
            if (it.shouldDoClose) {
                unmap()
            }
        }
    }

    override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
        DataAccessor.checkReadBounds(pos, buf, off, len)

        flag.getAndUpdate {
            if (it.closed) {
                throw IOException("already closed")
            }
            it.acquire()
        }

        try {
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

        } finally {
            if (flag.getAndUpdate { it.release() }.shouldDoClose) {
                unmap()
            }
        }
    }

}

