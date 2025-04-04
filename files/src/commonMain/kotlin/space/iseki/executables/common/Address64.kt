@file:Suppress("NOTHING_TO_INLINE", "unused")

package space.iseki.executables.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import space.iseki.executables.pe.decodeULongHex
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

inline fun Address64(value: Long) = Address64(value.toULong())
inline fun Address64(value: Int) = Address64(value.toULong())
inline fun Address64(value: UInt) = Address64(value.toULong())

/**
 * Represents a 64-bit address in a PE file.
 *
 * @property value the long value representing the address
 */
@Serializable(with = Address64.Serializer::class)
@JvmInline
value class Address64(val value: ULong) : Comparable<Address64> {

    /**
     * A serializer for [Address64].
     *
     * It serializes the address as a hex string, e.g. "0x123456789abcdef0".
     */
    object Serializer : KSerializer<Address64> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): Address64 {
            return Address64(decodeULongHex(decoder, "Address64").toLong())
        }

        override fun serialize(encoder: Encoder, value: Address64) {
            encoder.encodeString(value.toString())
        }

    }

    override fun compareTo(other: Address64): Int {
        return value.compareTo(other.value)
    }

    /**
     * Convert to string in hex format, e.g. "0x123456789abcdef0"
     *
     * @return a string in hex format
     */
    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "0x${value.toHexString()}"
    }

    companion object {
        /**
         * Converts the given raw long value to a hexadecimal string representation of a 64-bit address.
         *
         * @param value the raw long value
         * @return a string in hex format
         */
        @JvmStatic
        fun toString(value: Long): String {
            return Address64(value).toString()
        }
    }

    // Arithmetic Operators
    // region Plus
    inline operator fun plus(other: Int): Address64 = Address64(value + other.toULong())
    inline operator fun plus(other: Long): Address64 = Address64(value + other.toULong())
    inline operator fun plus(other: UInt): Address64 = Address64(value + other.toULong())
    inline operator fun plus(other: ULong): Address64 = Address64(value + other)
    // endregion

    // region Minus
    inline operator fun minus(other: Int): Address64 = Address64(value - other.toULong())
    inline operator fun minus(other: Long): Address64 = Address64(value - other.toULong())
    inline operator fun minus(other: UInt): Address64 = Address64(value - other.toULong())
    inline operator fun minus(other: ULong): Address64 = Address64(value - other)
    // endregion

    // region Modulo
    inline operator fun rem(other: Int): Address64 = Address64(value % other.toULong())
    inline operator fun rem(other: Long): Address64 = Address64(value % other.toULong())
    inline operator fun rem(other: UInt): Address64 = Address64(value % other.toULong())
    inline operator fun rem(other: ULong): Address64 = Address64(value % other)
    // endregion

    // region Utilities
    inline fun isAlignedTo(align: ULong): Boolean = value % align == 0UL

    inline fun alignUp(align: ULong): Address64 =
        if (isAlignedTo(align)) this else Address64((value + align - 1UL) / align * align)

    inline fun alignDown(align: ULong): Address64 = Address64(value / align * align)

// Bitwise Operations

    /** Inverts all bits of this address. */
    inline operator fun inv(): Address64 = Address64(this.value.inv())

    /** Performs bitwise AND with another [Address64]. */
    inline infix fun and(other: Address64): Address64 = Address64(this.value and other.value)

    /** Performs bitwise AND with a [Long]. */
    inline infix fun and(other: Long): Address64 = this and Address64(other)

    /** Performs bitwise AND with a [ULong]. */
    inline infix fun and(other: ULong): Address64 = this and Address64(other)

    /** Performs bitwise OR with another [Address64]. */
    inline infix fun or(other: Address64): Address64 = Address64(this.value or other.value)

    /** Performs bitwise OR with a [Long]. */
    inline infix fun or(other: Long): Address64 = this or Address64(other)

    /** Performs bitwise OR with a [ULong]. */
    inline infix fun or(other: ULong): Address64 = this or Address64(other)

    /** Performs bitwise XOR with another [Address64]. */
    inline infix fun xor(other: Address64): Address64 = Address64(this.value xor other.value)

    /** Performs bitwise XOR with a [Long]. */
    inline infix fun xor(other: Long): Address64 = this xor Address64(other)

    /** Performs bitwise XOR with a [ULong]. */
    inline infix fun xor(other: ULong): Address64 = this xor Address64(other)


// Shift Operations

    /** Shifts the bits of this address to the left by the given number of [bits]. */
    inline infix fun shl(bits: Int): Address64 = Address64(this.value shl bits)

    /** Shifts the bits of this address to the right by the given number of [bits]. */
    inline infix fun shr(bits: Int): Address64 = Address64(this.value shr bits)

    /**
     * converts this address to an integer.
     *
     * @return the integer representation of the address
     */
    inline fun toLong(): Long = value.toLong()

    /**
     * converts this address to an integer.
     *
     * @return the integer representation of the address
     */
    inline fun toULong(): ULong = value
}