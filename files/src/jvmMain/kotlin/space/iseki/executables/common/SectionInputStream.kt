package space.iseki.executables.common

import java.io.IOException
import java.io.InputStream

/**
 * An implementation of InputStream that reads data from a ReadableSection.
 * This class provides a way to read section data using the standard Java InputStream API.
 */
class SectionInputStream(private val section: ReadableSection) : InputStream() {
    private var position: Long = 0
    private val buffer = ByteArray(1)

    /**
     * Reads a single byte from the section.
     *
     * @return The byte read, as an integer in the range 0 to 255, or -1 if the end of the section has been reached.
     */
    override fun read(): Int {
        val bytesRead = read(buffer, 0, 1)
        return if (bytesRead == -1) -1 else buffer[0].toInt() and 0xFF
    }

    /**
     * Reads up to len bytes of data from the section into an array of bytes.
     *
     * @param b The buffer into which the data is read.
     * @param off The start offset in array b at which the data is written.
     * @param len The maximum number of bytes to read.
     * @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of the section has been reached.
     */
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len == 0) {
            return 0
        }

        val size = section.size
        // If we've reached the end of the section or the section size is unknown
        if (size >= 0 && position >= size) {
            return -1
        }

        try {
            // Calculate the actual number of bytes to read
            val bytesToRead = if (size >= 0) {
                minOf(len.toLong(), size - position).toInt()
            } else {
                len
            }

            if (bytesToRead <= 0) {
                return -1
            }

            // Read data from the section
            section.readBytes(position, b, off, bytesToRead)
            position += bytesToRead
            return bytesToRead
        } catch (e: Exception) {
            throw IOException("Error reading from section", e)
        }
    }

    /**
     * Skips over and discards n bytes of data from this input stream.
     *
     * @param n The number of bytes to be skipped.
     * @return The actual number of bytes skipped.
     */
    override fun skip(n: Long): Long {
        if (n <= 0) {
            return 0
        }

        val size = section.size
        val available = if (size >= 0) {
            size - position
        } else {
            Long.MAX_VALUE
        }

        val toSkip = minOf(available, n)
        position += toSkip
        return toSkip
    }

    /**
     * Returns an estimate of the number of bytes that can be read from this input stream without blocking.
     *
     * @return An estimate of the number of bytes that can be read without blocking.
     */
    override fun available(): Int {
        val size = section.size
        return if (size >= 0) {
            minOf(Int.MAX_VALUE.toLong(), size - position).toInt()
        } else {
            0
        }
    }
}

/**
 * Extension function that creates an InputStream from a ReadableSection.
 *
 * @return An InputStream that reads data from this section.
 */
inline fun ReadableSection.inputStream(): InputStream = SectionInputStream(this) 