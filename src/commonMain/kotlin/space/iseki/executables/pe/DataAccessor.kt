package space.iseki.executables.pe


internal interface DataAccessor : AutoCloseable {
    fun readAtMost(pos: Long, buf: ByteArray): Int
    fun readFully(pos: Long, buf: ByteArray) {
        var offset = 0
        while (offset < buf.size) {
            val read = readAtMost(pos + offset, buf)
            if (read == -1) {
                throw PEEOFException()
            }
            offset += read
        }
    }
}

