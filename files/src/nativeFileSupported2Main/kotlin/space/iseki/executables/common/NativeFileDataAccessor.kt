package space.iseki.executables.common

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
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

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal class NativeFileDataAccessor(private val nativePath: String) : DataAccessor {
    private val file: CPointer<FILE>
    private val beginPtr: COpaquePointer
    override val size: Long
    private val flag = atomic(CloseFlag())
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
                if (size > Int.MAX_VALUE) {
                    throw IOException("File size too large")
                }
                val pointer = mmap(null, stat.st_size.toUInt(), PROT_READ, MAP_SHARED, fileno(f), 0)
                if (pointer == MAP_FAILED) {
                    throw translateErrorImmediately("mmap")
                }
                beginPtr = pointer ?: throw Error("mmap() == NULL")
            }
        } catch (th: Throwable) {
            if (fclose(f) != 0) {
                th.addSuppressed(translateErrorImmediately("fclose"))
            }
            throw th
        }
        file = f
    }

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
            val available = (size - pos).coerceAtLeast(0).coerceAtMost(len.toLong()).toInt()
            if (available == 0) return 0

            buf.usePinned { pinned ->
                val src = beginPtr.reinterpret<ByteVar>().plus(pos)
                val dst = pinned.addressOf(off)
                memcpy(dst, src, available.toUInt())
            }

            return available

        } finally {
            if (flag.getAndUpdate { it.release() }.shouldDoClose) {
                unmap()
            }
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

    private fun unmap() {
        if (munmap(beginPtr, size.toUInt()) == -1) {
            throw translateErrorImmediately("munmap")
        }
    }

    override fun toString(): String = "NativeFileDataAccessor(path=$nativePath)"
}
