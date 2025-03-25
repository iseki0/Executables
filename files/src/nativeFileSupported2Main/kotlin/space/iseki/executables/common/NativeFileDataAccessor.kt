package space.iseki.executables.common

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import platform.posix.FILE
import platform.posix.SEEK_SET
import platform.posix.clearerr
import platform.posix.errno
import platform.posix.fclose
import platform.posix.feof
import platform.posix.ferror
import platform.posix.fileno
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.fstat
import platform.posix.stat

@OptIn(ExperimentalForeignApi::class)
internal class NativeFileDataAccessor(private val nativePath: String) : DataAccessor {
    private val file: CPointer<FILE>
    override val size: Long

    init {
        val f = fopen(nativePath, "rb")
        if (f == null) {
            val e = errno
            throw IOException("Cannot open file $nativePath, errno = $e")
        }
        memScoped {
            val stat = alloc<stat>()
            fstat(fileno(f), stat.ptr)
            size = stat.st_size
        }
        file = f
    }

    override fun close() {
        val e = fclose(file)
        if (e != 0) {
            val errno = errno
            throw IOException("Cannot close file $nativePath, errno = $errno")
        }
    }

    @OptIn(UnsafeNumber::class)
    override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
        if (len == 0) return 0
        // check ranges
        if (off < 0 || len < 0 || len > buf.size - off) {
            throw IndexOutOfBoundsException()
        }
        if (pos > Int.MAX_VALUE) throw IOException("file too large")
        val seekR = fseek(file, pos.toInt(), SEEK_SET)
        if (seekR != 0) {
            val e = errno
            clearerr(file)
            throw IOException("Cannot seek file $nativePath, seek = $seekR, errno = $e")
        }
        var p = off
        buf.usePinned { pinned ->
            while (p - off < len) {
                val ptr = pinned.addressOf(p)
                val count = len - (p - off)
                val read = fread(ptr, 1u, count.toUInt(), file).toInt()
                p += read
                if (read < count) {
                    if (feof(file) != 0) {
                        clearerr(file)
                        break
                    }
                    val e = ferror(file)
                    clearerr(file)
                    throw IOException("Cannot read file $nativePath, ferror = $e")
                }
            }
        }
        return p - off
    }

    override fun toString(): String = "NativeFileDataAccessor(path=$nativePath)"
}

