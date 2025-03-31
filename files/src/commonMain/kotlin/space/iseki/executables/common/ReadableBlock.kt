package space.iseki.executables.common

/**
 * Represents a block of data that can be read.
 *
 * This interface is used to read bytes from a block of data.
 */
interface ReadableBlock {

    /**
     * The size of the block in bytes.
     *
     * -1 if the size is unknown.
     */
    val size: Long
        get() = -1


    /**
     * Reads bytes from the block.
     *
     * If the specified range exceeds
     * the actual size of the block, only the available data within the block is copied, leaving
     * any extra space in the buffer unaltered.
     * @param off the offset within the block to read from
     * @param buf the buffer to read into
     * @param bufOff the offset within the buffer to read into
     * @param size the number of bytes to read
     *
     * @throws IndexOutOfBoundsException if the [bufOff] and the [size] is out of bounds
     */
    fun readBytes(off: Long, buf: ByteArray, bufOff: Int, size: Int)
}
