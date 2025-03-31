package space.iseki.executables.common

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
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
import platform.posix.FILE
import platform.posix.MAP_FAILED
import platform.posix.MAP_SHARED
import platform.posix.PROT_READ
import platform.posix.errno
import platform.posix.fclose
import platform.posix.fileno
import platform.posix.fopen
import platform.posix.fstat
import platform.posix.memcpy
import platform.posix.mmap
import platform.posix.munmap
import platform.posix.stat
import platform.posix.strerror
import space.iseki.executables.share.ClosableDataAccessor

@OptIn(ExperimentalForeignApi::class)
internal class NativeFileDataAccessor(private val nativePath: String) : ClosableDataAccessor() {
    private val file: CPointer<FILE>
    private val beginPtr: COpaquePointer?
    override val size: Long

    init {
        val f = fopen(nativePath, "rb") ?: throw translateErrorImmediately("fopen")
        try {
            memScoped {
                val fd = fileno(f)
                if (fd == -1) {
                    throw translateErrorImmediately("fileno")
                }
                val stat = alloc<stat>()
                if (fstat(fd, stat.ptr) == -1) {
                    throw translateErrorImmediately("fstat")
                }
                size = stat.st_size
                if (size > 0L) {
                    val pointer = mmap(null, stat.st_size.toULong(), PROT_READ, MAP_SHARED, fileno(f), 0)
                    if (pointer == MAP_FAILED) {
                        throw translateErrorImmediately("mmap")
                    }
                    beginPtr = pointer ?: throw Error("mmap() == NULL")
                } else {
                    beginPtr = null
                }
            }
        } catch (th: Throwable) {
            if (fclose(f) != 0) {
                th.addSuppressed(translateErrorImmediately("fclose"))
            }
            throw th
        }
        file = f
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
                memcpy(dst, src, available.toULong())
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
    override fun doClose() {
        var th: Throwable? = null
        if (beginPtr != null) {
            if (munmap(beginPtr, size.toULong()) == -1) {
                translateErrorImmediately("munmap").also {
                    th?.addSuppressed(it) ?: run { th = it }
                }
            }
        }
        if (fclose(file) != 0) {
            translateErrorImmediately("fclose").also {
                th?.addSuppressed(it) ?: run { th = it }
            }
        }
        th?.let { throw it }
    }
}

