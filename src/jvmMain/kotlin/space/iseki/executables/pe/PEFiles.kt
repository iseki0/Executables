@file:JvmName("PEFiles")
@file:JvmMultifileClass

package space.iseki.executables.pe

import java.io.File
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@JvmOverloads
@JvmName("open")
fun PEFile(channel: SeekableByteChannel, closeWhenTheFileClosed: Boolean = true): PEFile {
    val accessor = object : DataAccessor {
        override fun close() {
            if (closeWhenTheFileClosed) {
                channel.close()
            }
        }

        override fun toString(): String {
            return "DataAccessor(channel=$channel)"
        }

        override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
            val buffer = java.nio.ByteBuffer.wrap(buf, off, len)
            channel.position(pos)
            while (buffer.hasRemaining()) {
                val i = channel.read(buffer)
                if (i == -1) break
            }
            return buffer.position()
        }
    }
    return PEFile.open(accessor)
}

@JvmName("open")
fun PEFile(file: File): PEFile {
    val input = file.inputStream()
    try {
        return PEFile(input.channel)
    } catch (th: Throwable) {
        try {
            input.close()
        } catch (th1: Throwable) {
            th.addSuppressed(th1)
        }
        throw th
    }
}

@JvmName("open")
fun PEFile(path: Path): PEFile {
    val channel = Files.newByteChannel(path, StandardOpenOption.READ)
    try {
        return PEFile(channel)
    } catch (th: Throwable) {
        try {
            channel.close()
        } catch (th1: Throwable) {
            th.addSuppressed(th1)
        }
        throw th
    }
}
