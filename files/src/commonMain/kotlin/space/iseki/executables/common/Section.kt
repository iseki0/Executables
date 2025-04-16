package space.iseki.executables.common

/**
 * Represents a section of a file that can be read.
 *
 */
interface ReadableSection : DataAccessor {
    /**
     * The size of the section in bytes.
     *
     * -1 if the size is unknown.
     */
    override val size: Long
        get() = -1

    /**
     * The name of the section, if exists.
     */
    val name: String?
        get() = null

    /**
     * The header of the section, if exists.
     *
     * The header is a structure that describes the section.
     */
    val header: ReadableStructure?
        get() = null

    /**
     * Reads bytes from the section.
     *
     * If the specified range exceeds
     * the actual size of the section, only the available data within the section is copied, leaving
     * any extra space in the buffer unaltered.
     * @param sectionOffset the offset within the section to read from
     * @param buf the buffer to read into
     * @param bufOffset the offset within the buffer to read into
     * @param size the number of bytes to read
     *
     * @throws IOException if an I/O error occurs
     * @throws IndexOutOfBoundsException if the [bufOffset] and the [size] is out of bounds
     */
    fun readBytes(sectionOffset: Long, buf: ByteArray, bufOffset: Int, size: Int) {
        checkReadBytesBounds(sectionOffset, buf, bufOffset, size)
        readFully(sectionOffset, buf, bufOffset, size)

    }

    companion object {
        fun checkReadBytesBounds(sectionOffset: Long, buf: ByteArray, off: Int, len: Int) {
            DataAccessor.checkReadBounds(sectionOffset, buf, off, len)
            if (sectionOffset + buf.size < len) {
                throw IndexOutOfBoundsException("Read exceeds section size: pos=$sectionOffset, buf.size=${buf.size}, len=$len")
            }
        }
    }

    /**
     * No-op close method.
     */
    override fun close() {}
}
