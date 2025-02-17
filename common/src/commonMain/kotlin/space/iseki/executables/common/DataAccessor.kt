package space.iseki.executables.common

/**
 * Defines an interface for random access data reading.
 * Implementations should provide methods to read bytes from a data source.
 */
 
interface DataAccessor : AutoCloseable {
    /**
     * Reads at most [len] bytes from the file at the specified [pos] into the [buf] array.
     *
     * @throws IOException if an I/O error occurs
     * @return the number of bytes read, or -1 if the end of the file is reached
     */
    @Throws(IOException::class)
    fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int

    /**
     * Reads at most [buf].size bytes from the file at the specified [pos] into the [buf] array.
     *
     * @throws IOException if an I/O error occurs
     * @return the number of bytes read, or -1 if the end of the file is reached
     */
    @Throws(IOException::class)
    fun readAtMost(pos: Long, buf: ByteArray): Int = readAtMost(pos, buf, 0, buf.size)

    /**
     * @throws IOException if an I/O error occurs
     * @throws EOFException if the end of the file is reached before reading all bytes that fulfilled the request
     */
    @Throws(IOException::class)
    fun readFully(pos: Long, buf: ByteArray) = readFully(pos, buf, 0, buf.size)

    /**
     * Reads exactly [len] bytes from the file at the specified [pos] into the [buf] array.
     *
     * @throws IOException if an I/O error occurs
     * @throws EOFException if the end of the file is reached before reading all bytes that fulfilled the request
     */
    @Throws(IOException::class)
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

