package space.iseki.executables.common

/**
 * A [DataAccessor] implementation that reads data from a [ByteArray].
 *
 * @param data the source byte array from which data is read.
 */
internal class ByteArrayDataAccessor(private val data: ByteArray) : DataAccessor {
    override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
        DataAccessor.checkReadBounds(pos, buf, off, len)
        val read = (data.size - pos).coerceAtLeast(0).coerceAtMost(len.toLong()).toInt()
        if (read == 0) return 0
        data.copyInto(buf, destinationOffset = off, startIndex = pos.toInt(), endIndex = pos.toInt() + read)
        return read
    }

    /**
     * No-op close method.
     */
    override fun close() {
    }

    override fun toString(): String {
        return "ByteArrayDataAccessor(data=$data)"
    }

    override val size: Long
        get() = data.size.toLong()
}
