package space.iseki.executables


interface DataAccessor : AutoCloseable {
    fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int
    fun readAtMost(pos: Long, buf: ByteArray): Int = readAtMost(pos, buf, 0, buf.size)
    fun readFully(pos: Long, buf: ByteArray) = readFully(pos, buf, 0, buf.size)

    fun readFully(pos: Long, buf: ByteArray, off: Int, len: Int) {
        var offset = 0
        while (offset < len) {
            val read = readAtMost(pos + offset, buf, off + offset, len - offset)
            if (read == -1) {
                throw EOFException("Unexpected EOF reached")
            }
            offset += read
        }
    }

}

