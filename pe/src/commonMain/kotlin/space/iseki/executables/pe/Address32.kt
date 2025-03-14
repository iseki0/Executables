package space.iseki.executables.pe

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

/**
 * Convert from raw value to 32-bit address.
 *
 * @param value raw value
 * @return 32-bit address
 */
fun Address32(value: Int): Address32 = Address32(value.toUInt())

/**
 * Represents a 32-bit address in a PE file.
 *
 * @property value the unsigned integer representing the address
 */
@Serializable(with = Address32.Serializer::class)
@JvmInline
value class Address32(val value: UInt) : Comparable<Address32> {

    /**
     * A serializer for [Address32].
     *
     * It serializes the address as a hex string, e.g. "0x12345678".
     */
    object Serializer : KSerializer<Address32> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): Address32 {
            val v = decodeULongHex(decoder, "Address32")
            if (v > UInt.MAX_VALUE) {
                throw SerializationException("Address32 should be less than 0x100000000")
            }
            return Address32(v.toUInt())
        }

        override fun serialize(encoder: Encoder, value: Address32) {
            encoder.encodeString(value.toString())
        }

    }

    override fun compareTo(other: Address32): Int {
        return value.compareTo(other.value)
    }

    /**
     * Convert to string in hex format, e.g. "0x12345678"
     *
     * @return string in hex format
     */
    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "0x${value.toHexString()}"
    }

    companion object {
        /**
         * converts the given raw integer value to a hexadecimal string representation of a 32-bit address.
         *
         * @param value the raw integer value
         * @return a string in hex format
         */
        @JvmStatic
        fun toString(value: Int): String {
            return Address32(value.toUInt()).toString()
        }
    }

    /**
     * adds the given address to this address.
     *
     * @param other the address to add
     * @return the summed address
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun plus(other: Address32): Address32 = Address32(this.value.plus(other.value))

    /**
     * adds the given integer (treated as an address) to this address.
     *
     * @param other the integer value to add
     * @return the summed address
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun plus(other: Int): Address32 = this + Address32(other)

    /**
     * adds the given unsigned integer (treated as an address) to this address.
     *
     * @param other the unsigned integer value to add
     * @return the summed address
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun plus(other: UInt): Address32 = this + Address32(other)

    /**
     * subtracts the given address from this address.
     *
     * @param other the address to subtract
     * @return the resulting address
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun minus(other: Address32): Address32 = Address32(this.value.minus(other.value))

    /**
     * returns the bitwise and of this address and the given address.
     *
     * @param other the address to perform bitwise and with
     * @return the result of the bitwise and
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun and(other: Address32): Address32 = Address32(this.value and other.value)

    /**
     * returns the bitwise and of this address and the given unsigned integer.
     *
     * @param other the unsigned integer to perform bitwise and with
     * @return the result of the bitwise and
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun and(other: UInt): Address32 = Address32(this.value and other)

    /**
     * converts this address to an integer.
     *
     * @return the integer representation of the address
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun toInt(): Int = value.toInt()
}
