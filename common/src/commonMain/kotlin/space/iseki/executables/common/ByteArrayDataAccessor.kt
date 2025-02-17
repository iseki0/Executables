package space.iseki.executables.common

/**
 * A [DataAccessor] implementation that reads data from a [ByteArray].
 *
 * @param data the source byte array from which data is read.
 */
class ByteArrayDataAccessor(private val data: ByteArray) : DataAccessor {
    override fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int {
        if (pos < 0 || pos >= data.size) {
            return -1
        }
        val read = (data.size - pos).coerceAtMost(len.toLong()).toInt()
        data.copyInto(buf, off, pos.toInt(), pos.toInt() + read)
        return read
    }

    /**
     * No-op close method.
     */
    override fun close() {
    }
}
