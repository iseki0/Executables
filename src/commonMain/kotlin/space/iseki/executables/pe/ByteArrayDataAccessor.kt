package space.iseki.executables.pe

class ByteArrayDataAccessor(private val data: ByteArray) : DataAccessor {
    override fun readAtMost(pos: Long, buf: ByteArray): Int {
        if (pos < 0 || pos >= data.size) {
            return -1
        }
        val read = (data.size - pos).coerceAtMost(buf.size.toLong()).toInt()
        data.copyInto(buf, 0, pos.toInt(), pos.toInt() + read)
        return read
    }

    override fun close() {
    }
}
