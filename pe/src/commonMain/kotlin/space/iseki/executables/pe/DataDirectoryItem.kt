package space.iseki.executables.pe

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

/**
 * Represents a data directory item in a pe file.
 *
 * the raw value is a long combining the virtual address (upper 32 bits) and the size (lower 32 bits).
 *
 * @property rawValue the combined raw value
 */
@Serializable(with = DataDirectoryItem.Serializer::class)
@JvmInline
value class DataDirectoryItem(
    private val rawValue: Long,
) {
    object Serializer : KSerializer<DataDirectoryItem> {
        @Serializable
        private data class DTO(
            val virtualAddress: Address32,
            val size: UInt,
        )

        override val descriptor: SerialDescriptor = DTO.serializer().descriptor

        override fun deserialize(decoder: Decoder): DataDirectoryItem {
            decoder.decodeSerializableValue(DTO.serializer()).let {
                val v = it.virtualAddress.rawValue.toLong() shl 32 or (it.size.toLong() and 0xFFFFFFFFL)
                return DataDirectoryItem(v)
            }
        }

        override fun serialize(encoder: Encoder, value: DataDirectoryItem) {
            encoder.encodeSerializableValue(
                serializer = DTO.serializer(),
                value = DTO(value.virtualAddress, value.size.toUInt()),
            )
        }

    }

    /**
     * Secondary constructor that creates a data directory item from a virtual address and size.
     *
     * @param virtualAddress the virtual address part
     * @param size the size part
     */
    constructor(virtualAddress: Int, size: Int) : this((virtualAddress.toLong() shl 32) or size.toLong())

    companion object {
        /**
         * A zero data directory item.
         */
        val ZERO = DataDirectoryItem(0)

        /**
         * Creates a data directory item from unsigned virtual address and size.
         *
         * @param virtualAddress the virtual address as unsigned integer
         * @param size the size as unsigned integer
         * @return a data directory item
         */
        operator fun invoke(virtualAddress: UInt, size: UInt): DataDirectoryItem {
            return DataDirectoryItem(virtualAddress.toInt(), size.toInt())
        }

        /**
         * Converts the given raw value to its string representation as a data directory item.
         *
         * @param rawValue the raw long value
         * @return a string representation
         */
        @JvmStatic
        fun toString(rawValue: Long): String {
            return DataDirectoryItem(rawValue).toString()
        }

        /**
         * Parses a data directory item from the given byte array starting at the specified offset.
         *
         * @param bytes the byte array containing the data
         * @param offset the offset at which the data directory item starts
         * @return a parsed data directory item
         */
        @JvmStatic
        fun parse(bytes: ByteArray, offset: Int): DataDirectoryItem {
            val virtualAddress = bytes.getUInt(offset)
            val size = bytes.getUInt(offset + 4)
            return DataDirectoryItem(virtualAddress, size)
        }
    }

    /**
     * Retrieves the virtual address part of the data directory item.
     *
     * @return the virtual address as an [Address32]
     */
    val virtualAddress: Address32
        get() = Address32((rawValue ushr 32).toInt())

    /**
     * Retrieves the size part of the data directory item.
     *
     * @return the size as an integer
     */
    val size: Int
        get() = rawValue.toInt()

    /**
     * Returns the string representation of the data directory item.
     * if it is zero, returns "ZERO"; otherwise, returns a formatted string.
     *
     * @return a string representation
     */
    override fun toString(): String {
        if (this == ZERO) {
            return "ZERO"
        }
        return "[$virtualAddress + $size]"
    }
}

