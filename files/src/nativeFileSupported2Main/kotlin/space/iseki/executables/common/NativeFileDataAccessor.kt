package space.iseki.executables.common

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.plus
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.usePinned
import platform.posix.EACCES
import platform.posix.ENOENT
import platform.posix.EPERM
import platform.posix.MAP_FAILED
import platform.posix.MAP_SHARED
import platform.posix.O_CLOEXEC
import platform.posix.O_RDONLY
import platform.posix.PROT_READ
import platform.posix.errno
import platform.posix.fstat
import platform.posix.memcpy
import platform.posix.mmap
import platform.posix.munmap
import platform.posix.open
import platform.posix.stat
import platform.posix.strerror
import space.iseki.executables.share.ClosableDataAccessor
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner
import kotlin.native.terminateWithUnhandledException

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
@OptIn(ExperimentalNativeApi::class)
internal actual fun openNativeFileDataAccessor(path: String): DataAccessor {
    val impl = NativeFileDataAccessorImpl(path)
    return object : DataAccessor by impl {
        val cleaner = createCleaner(impl, AutoCloseable::close)
    }
}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
private class NativeFileDataAccessorImpl(private val nativePath: String) : ClosableDataAccessor() {
    private val beginPtr: COpaquePointer?
    override val size: Long

    init {
        val fd = open(nativePath, O_CLOEXEC or O_RDONLY)
        if (fd == -1) throw translateErrorImmediately("open")
        try {
            memScoped {
                val stat = alloc<stat>()
                if (fstat(fd, stat.ptr) == -1) {
                    throw translateErrorImmediately("fstat")
                }
                size = stat.st_size
                if (size > 0L) {
                    val pointer = mmap(null, stat.st_size.toUInt(), PROT_READ, MAP_SHARED, fd, 0)
                    if (pointer == MAP_FAILED) {
                        throw translateErrorImmediately("mmap")
                    }
                    beginPtr = pointer ?: throw Error("mmap() == NULL")
                } else {
                    beginPtr = null
                }
            }
        } finally {
            platform.posix.close(fd)
        }
    }

    override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
        DataAccessor.checkReadBounds(pos, buf, off, len)
        wrapRead {
            val beginPtr = beginPtr ?: return 0
            val available = (size - pos).coerceAtLeast(0).coerceAtMost(len.toLong()).toInt()
            if (available == 0) return 0

            buf.usePinned { pinned ->
                val src = beginPtr.reinterpret<ByteVar>().plus(pos)
                val dst = pinned.addressOf(off)
                memcpy(dst, src, available.toUInt())
            }
            return available
        }
    }

    private fun translateErrorImmediately(nativeCall: String): IOException {
        val errorCode = errno
        val m = strerror(errorCode)?.toKStringFromUtf8().orEmpty()
        val msg = listOfNotNull(
            if (nativeCall.isNotEmpty()) "nativeCall: $nativeCall" else null,
            "errno: $errorCode",
            if (m.isNotEmpty()) "os_message: $m" else null,
        ).joinToString(", ")
        return when (errorCode) {
            ENOENT -> NoSuchFileException(nativePath, null, msg)
            EACCES, EPERM -> AccessDeniedException(nativePath, null, msg)
            else -> IOException("Cannot open file $nativePath, $msg")
        }
    }

    override fun toString(): String = "NativeFileDataAccessor(path=$nativePath)"
    @OptIn(ExperimentalNativeApi::class)
    override fun doClose() {
        if (beginPtr == null) return
        if (munmap(beginPtr, size.toUInt()) == -1) {
            val errorCode = errno
            val errorMessage = strerror(errorCode)?.toKStringFromUtf8().orEmpty()
            val ise = IllegalStateException("munmap failed: errno=$errorCode, message=$errorMessage")
            terminateWithUnhandledException(ise)
        }
    }
}
