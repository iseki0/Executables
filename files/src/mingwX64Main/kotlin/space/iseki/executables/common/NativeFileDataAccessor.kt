package space.iseki.executables.common

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readValue
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.windows.CloseHandle
import platform.windows.CreateFileW
import platform.windows.DWORD
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.FILE_BEGIN
import platform.windows.FILE_FLAG_RANDOM_ACCESS
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
import platform.windows.LPWSTRVar
import platform.windows.LocalFree
import platform.windows.OPEN_EXISTING
import platform.windows.ReadFile
import platform.windows.SUBLANG_DEFAULT
import platform.windows.SetFilePointerEx

@OptIn(ExperimentalForeignApi::class)
internal class NativeFileDataAccessor(private val nativePath: String) : DataAccessor {
    private val handle: HANDLE
    override val size: Long

    init {
        val file: CPointer<out CPointed>? = CreateFileW(
            lpFileName = nativePath,
            dwDesiredAccess = GENERIC_READ,
            dwShareMode = (FILE_SHARE_READ or FILE_SHARE_WRITE or FILE_SHARE_DELETE).toUInt(),
            lpSecurityAttributes = null,
            dwCreationDisposition = OPEN_EXISTING.toUInt(),
            dwFlagsAndAttributes = (FILE_ATTRIBUTE_NORMAL or FILE_FLAG_RANDOM_ACCESS).toUInt(),
            hTemplateFile = null,
        )
        if (file == INVALID_HANDLE_VALUE) {
            val errorCode = GetLastError()
            throw IOException("Cannot open file $nativePath, errno = $errorCode, message: ${translateErrorCode(errorCode)}")
        }
        handle = checkNotNull(file) { "CreateFileW returns null" }
        try {
            size = getFileSize(handle)
        } catch (th: Throwable) {
            runCatching { close() }.onFailure { th.addSuppressed(it) }
            throw th
        }
    }

    private fun makeLangId(primary: Int, sub: Int) = (sub shl 10) or primary
    private fun getFileSize(handle: HANDLE): Long {
        memScoped {
            val size = alloc<LARGE_INTEGER>()
            val success = GetFileSizeEx(handle, size.ptr)
            if (success == 0) {
                val err = GetLastError()
                throw IOException("GetFileSizeEx failed: ${translateErrorCode(err)}")
            }
            return size.QuadPart
        }
    }

    private fun translateErrorCode(errorCode: DWORD): String {
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

    override fun toString(): String = "NativeFileDataAccessor[MingW64](path=$nativePath)"
    override fun close() {
        val r = CloseHandle(handle)
        if (r == 0) {
            val err = GetLastError()
            throw IOException("Close failed: ${translateErrorCode(err)}")
        }
    }

    override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
        if (len == 0) return 0
        // check ranges
        if (off < 0 || len < 0 || len > buf.size - off) {
            throw IndexOutOfBoundsException()
        }
        memScoped {
            val newPos = alloc<LARGE_INTEGER>().apply { QuadPart = pos }
            val actualPos = alloc<LARGE_INTEGER>()
            val seekOk = newPos.usePinned {
                SetFilePointerEx(handle, newPos.readValue(), actualPos.ptr, FILE_BEGIN.toUInt())
            }
            if (seekOk == 0) {
                val err = GetLastError()
                throw IOException("Seek failed: ${translateErrorCode(err)}")
            }
            var p = off
            var continueRead = true
            while (p - off < len && continueRead) {
                buf.usePinned { pinned ->
                    val ptr = pinned.addressOf(p)
                    val count = len - (p - off)
                    val read = alloc<UIntVar>()
                    val readOk = ReadFile(handle, ptr, count.toUInt(), read.ptr, null)
                    if (readOk == 0) {
                        val err = GetLastError()
                        throw IOException("Read failed: ${translateErrorCode(err)}")
                    }
                    p += read.value.toInt()
                    if (read.value == 0u) {
                        continueRead = false
                    }
                }
            }
            return p - off
        }
    }

}

