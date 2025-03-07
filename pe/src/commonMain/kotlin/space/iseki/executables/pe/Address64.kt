package space.iseki.executables.pe

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

fun Address64(value: ULong) = Address64(value.toLong())

/**
 * Represents a 64-bit address in a PE file.
 *
 * @property value the long value representing the address
 */
@Serializable(with = Address64.Serializer::class)
@JvmInline
value class Address64(val value: Long) : Comparable<Address64> {

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
        return value.toULong().compareTo(other.value.toULong())
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
}