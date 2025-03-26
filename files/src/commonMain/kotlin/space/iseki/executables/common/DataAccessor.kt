package space.iseki.executables.common

/**
 * Defines an interface for random-access data reading.
 *
 * Implementations should support reading bytes from a random-access data source (e.g., memory, file, mapped region).
 * The interface guarantees consistent behavior across different sources, including strict parameter validation.
 */
interface DataAccessor : AutoCloseable {

    /**
     * Reads at most [len] bytes from the data source starting at the given [pos] into the [buf] array,
     * starting at offset [off].
     *
     * This function attempts to read up to [len] bytes into the buffer. Fewer bytes may be read only if
     * the end of the data is reached. This method never blocks indefinitely.
     *
     * @param pos The starting position in the data source (must be ≥ 0; values beyond data size are treated as EOF)
     * @param buf The destination byte array
     * @param off The starting offset in the buffer (must be in [0, buf.size))
     * @param len The maximum number of bytes to read (must be in [0, buf.size - off])
     *
     * @return The number of bytes actually read, or 0 if the end of data is reached
     *
     * @throws IndexOutOfBoundsException If [pos] < 0, or if [off]/[len] are invalid for [buf]
     * @throws IOException If an I/O error occurs (for file/network-backed implementations)
     */
    @Throws(IOException::class)
    fun readAtMost(pos: Long, buf: ByteArray, off: Int, len: Int): Int

    /**
     * Reads at most [buf.size] bytes from the data source starting at [pos] into the given [buf].
     *
     * Equivalent to: `readAtMost(pos, buf, 0, buf.size)`
     *
     * @throws IndexOutOfBoundsException If [pos] < 0
     * @throws IOException If an I/O error occurs
     */
    @Throws(IOException::class)
    fun readAtMost(pos: Long, buf: ByteArray): Int = readAtMost(pos, buf, 0, buf.size)

    /**
     * Reads exactly [buf.size] bytes starting at [pos] into the buffer.
     *
     * This method repeatedly invokes [readAtMost] until the buffer is fully filled.
     *
     * @throws EOFException If the end of data is reached before reading all bytes
     * @throws IndexOutOfBoundsException If [pos] < 0
     * @throws IOException If an I/O error occurs
     */
    @Throws(IOException::class, EOFException::class)
    fun readFully(pos: Long, buf: ByteArray) = readFully(pos, buf, 0, buf.size)

    /**
     * Reads exactly [len] bytes starting at [pos] into [buf], beginning at offset [off].
     *
     * This method attempts to read until the requested region of the buffer is fully filled.
     *
     * @throws EOFException If the end of data is reached before reading all bytes
     * @throws IndexOutOfBoundsException If any of [pos], [off], or [len] are invalid
     * @throws IOException If an I/O error occurs
     */
    @Throws(IOException::class, EOFException::class)
    fun readFully(pos: Long, buf: ByteArray, off: Int, len: Int) {
        checkReadBounds(pos, buf, off, len)
        var offset = 0
        while (offset < len) {
            val read = readAtMost(pos + offset, buf, off + offset, len - offset)
            if (read == 0) throw EOFException("End of data reached before reading $len bytes")
            offset += read
        }
    }

    /**
     * Checks whether the arguments to a read operation are valid.
     * This method is intended for internal use in implementing [readAtMost] or [readFully].
     *
     * @throws IndexOutOfBoundsException If [pos] is negative, or [off]/[len] are invalid for [buf]
     */
    companion object {
        fun checkReadBounds(pos: Long, buf: ByteArray, off: Int, len: Int) {
            if (pos < 0) {
                throw IndexOutOfBoundsException("Position must be non-negative: $pos")
            }
            if (off < 0 || len < 0 || len > buf.size - off) {
                throw IndexOutOfBoundsException("Invalid offset/length: off=$off, len=$len, buf.size=${buf.size}")
            }
        }
    }

    /**
     * The total size (in bytes) of the data accessible through this accessor.
     */
    val size: Long
}
