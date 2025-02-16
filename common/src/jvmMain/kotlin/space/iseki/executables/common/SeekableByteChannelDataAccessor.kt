package space.iseki.executables.common

import java.nio.channels.SeekableByteChannel

/**
 * A [DataAccessor] implementation that reads data from a [SeekableByteChannel].
 * @param channel the channel to read from
 * @see SeekableByteChannel
 * @see ByteArrayDataAccessor
 *
 */
open class SeekableByteChannelDataAccessor(private val channel: SeekableByteChannel) : DataAccessor {

    /**
     * Close the data accessor, it will close the underlying channel.
     *
     * @throws java.io.UncheckedIOException if an I/O error occurs
     * @see SeekableByteChannel.close
     */
    override fun close() {
        try {
            channel.close()
        } catch (e: java.io.IOException) {
            throw java.io.UncheckedIOException(e)
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
