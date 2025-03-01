package space.iseki.executables.common

import java.io.File
import java.io.RandomAccessFile

/**
 * A [DataAccessor] implementation that reads data from a [File].
 * @param file the file to read from
 * @see File
 * @see ByteArrayDataAccessor
 *
 */
internal open class RandomAccessFileDataAccessor : DataAccessor {
    private val raf: RandomAccessFile
    private val file: File?

    constructor(raf: RandomAccessFile) {
        this.raf = raf
        this.file = null
    }

    constructor(file: File) {
        this.raf = RandomAccessFile(file, "r")
        this.file = file
    }


    override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
        raf.seek(pos)

        var totalRead = 0
        var currentOffset = off
        var remaining = len

        while (remaining > 0) {
            val bytesRead = raf.read(buf, currentOffset, remaining)
            if (bytesRead == -1) break  // End of file reached

            totalRead += bytesRead
            currentOffset += bytesRead
            remaining -= bytesRead
        }

        return totalRead
    }

    override fun close() {
        try {
            raf.close()
        } catch (e: java.io.IOException) {
            throw java.io.UncheckedIOException(e)
        }
    }

    override fun toString(): String = "RandomAccessFileDataAccessor(raf=$raf, file=$file)"
}

