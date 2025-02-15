package space.iseki.executables

import java.nio.channels.SeekableByteChannel

open class SeekableByteChannelDataAccessor(private val channel: SeekableByteChannel) : DataAccessor {
    override fun close() {
        channel.close()
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
