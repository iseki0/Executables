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

fun Address32(rawValue: Int): Address32 = Address32(rawValue.toUInt())

@Serializable(with = Address32.Serializer::class)
@JvmInline
value class Address32(val rawValue: UInt) : Comparable<Address32> {
    object Serializer : KSerializer<Address32> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): Address32 {
            val v = decodeLongHex(decoder, "Address32")
            if (v.toULong() > UInt.MAX_VALUE) {
                throw SerializationException("Address32 should be less than 0x100000000")
            }
            return Address32(v.toUInt())
        }

        override fun serialize(encoder: Encoder, value: Address32) {
            encoder.encodeString(value.toString())
        }

    }

    override fun compareTo(other: Address32): Int {
        return rawValue.toUInt().compareTo(other.rawValue.toUInt())
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "0x${rawValue.toHexString()}"
    }

    companion object {
        @JvmStatic
        fun toString(rawValue: Int): String {
            return Address32(rawValue.toUInt()).toString()
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun plus(other: Address32): Address32 = Address32(this.rawValue.plus(other.rawValue))

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun plus(other: Int): Address32 = this + Address32(other)

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun plus(other: UInt): Address32 = this + Address32(other)

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun minus(other: Address32): Address32 = Address32(this.rawValue.minus(other.rawValue))

    @Suppress("NOTHING_TO_INLINE")
    inline infix fun and(other: Address32): Address32 = Address32(this.rawValue and other.rawValue)

    @Suppress("NOTHING_TO_INLINE")
    inline infix fun and(other: UInt): Address32 = Address32(this.rawValue and other)

    @Suppress("NOTHING_TO_INLINE")
    inline fun toInt(): Int = rawValue.toInt()
}
