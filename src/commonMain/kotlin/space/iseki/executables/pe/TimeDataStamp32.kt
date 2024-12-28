package space.iseki.executables.pe

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@Serializable(with = TimeDataStamp32.Serializer::class)
@JvmInline
value class TimeDataStamp32(val rawValue: UInt) : Comparable<TimeDataStamp32> {
    object Serializer : KSerializer<TimeDataStamp32> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): TimeDataStamp32 {
            return TimeDataStamp32(Instant.parse(decoder.decodeString()).epochSeconds.toUInt())
        }

        override fun serialize(encoder: Encoder, value: TimeDataStamp32) {
            encoder.encodeString(value.toString())
        }

    }

    override fun compareTo(other: TimeDataStamp32): Int {
        return rawValue.toUInt().compareTo(other.rawValue.toUInt())
    }

    override fun toString(): String {
        return Instant.fromEpochSeconds(rawValue.toUInt().toLong()).toString()
    }

    companion object {
        @JvmStatic
        fun toString(rawValue: Int): String {
            return TimeDataStamp32(rawValue.toUInt()).toString()
        }
    }
}
