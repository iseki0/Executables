@file:Suppress("NOTHING_TO_INLINE", "unused")

package space.iseki.executables.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import space.iseki.executables.pe.decodeULongHex
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

/**
 * Convert from raw value to 32-bit address.
 *
 * @param value raw value
 * @return 32-bit address
 */
inline fun Address32(value: Int): Address32 = Address32(value.toUInt())

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
    // --- Arithmetic Operators ---

    // region Plus
    inline operator fun plus(other: Int): Address32 = Address32(value + other.toUInt())
    inline operator fun plus(other: UInt): Address32 = Address32(value + other)
    // endregion

    // region Minus
    inline operator fun minus(other: Int): Address32 = Address32(value - other.toUInt())
    inline operator fun minus(other: UInt): Address32 = Address32(value - other)
    // endregion

    // region Modulo
    inline operator fun rem(other: Int): Address32 = Address32(value % other.toUInt())
    inline operator fun rem(other: UInt): Address32 = Address32(value % other)
    // endregion

    // region Utilities
    inline fun isAlignedTo(align: UInt): Boolean = value % align == 0U

    inline fun alignUp(align: UInt): Address32 =
        if (isAlignedTo(align)) this else Address32((value + align - 1U) / align * align)

    inline fun alignDown(align: UInt): Address32 = Address32(value / align * align)

    // endregion

    // --- Bitwise Operations ---

    /** Inverts all bits of this address. */
    inline operator fun inv(): Address32 = Address32(this.value.inv())

    /** Performs bitwise AND with another [Address32]. */
    inline infix fun and(other: Address32): Address32 = Address32(this.value and other.value)

    /** Performs bitwise AND with an [Int]. */
    inline infix fun and(other: Int): Address32 = this and Address32(other.toUInt())

    /** Performs bitwise AND with a [UInt]. */
    inline infix fun and(other: UInt): Address32 = this and Address32(other)

    /** Performs bitwise OR with another [Address32]. */
    inline infix fun or(other: Address32): Address32 = Address32(this.value or other.value)

    /** Performs bitwise OR with an [Int]. */
    inline infix fun or(other: Int): Address32 = this or Address32(other.toUInt())

    /** Performs bitwise OR with a [UInt]. */
    inline infix fun or(other: UInt): Address32 = this or Address32(other)

    /** Performs bitwise XOR with another [Address32]. */
    inline infix fun xor(other: Address32): Address32 = Address32(this.value xor other.value)

    /** Performs bitwise XOR with an [Int]. */
    inline infix fun xor(other: Int): Address32 = this xor Address32(other.toUInt())

    /** Performs bitwise XOR with a [UInt]. */
    inline infix fun xor(other: UInt): Address32 = this xor Address32(other)

    // --- Shift Operations ---

    /** Shifts the bits of this address to the left by the given number of [bits]. */
    inline infix fun shl(bits: Int): Address32 = Address32(this.value shl bits)

    /** Shifts the bits of this address to the right by the given number of [bits]. */
    inline infix fun shr(bits: Int): Address32 = Address32(this.value shr bits)

    /**
     * converts this address to an integer.
     *
     * @return the integer representation of the address
     */
    inline fun toInt(): Int = value.toInt()

    /**
     * converts this address to an integer.
     *
     * @return the integer representation of the address
     */
    inline fun toUInt(): UInt = value
}
