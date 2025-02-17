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

/**
 * Represents a 32-bit time stamp in PE/COFF file format.
 *
 * The low 32 bits of the number of seconds since 00:00 January 1, 1970 (a C run-time time_t value),
 * which indicates when the file was created.
 *
 * This value is commonly used in various PE/COFF structures:
 * - COFF File Header: indicates when the file was created
 * - Import Directory: date/time stamp of the DLL to import
 * - Debug Directory: date/time stamp of the debug information
 *
 * @property rawValue The raw 32-bit value representing seconds since Unix epoch (00:00 January 1, 1970).
 */
@Serializable(with = TimeDateStamp32.Serializer::class)
@JvmInline
value class TimeDateStamp32(val rawValue: UInt) : Comparable<TimeDateStamp32> {
    object Serializer : KSerializer<TimeDateStamp32> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): TimeDateStamp32 {
            return TimeDateStamp32(Instant.parse(decoder.decodeString()).epochSeconds.toUInt())
        }

        override fun serialize(encoder: Encoder, value: TimeDateStamp32) {
            encoder.encodeString(value.toString())
        }

    }

    override fun compareTo(other: TimeDateStamp32): Int {
        return rawValue.compareTo(other.rawValue)
    }

    /**
     * Converts the time stamp to its string representation in ISO-8601 format.
     *
     * @return The ISO-8601 formatted date-time string representing this time stamp.
     */
    override fun toString(): String {
        return Instant.fromEpochSeconds(rawValue.toLong()).toString()
    }

    companion object {
        /**
         * Converts a raw time stamp value to its string representation in ISO-8601 format.
         *
         * @param rawValue The raw time stamp value as Int.
         * @return The ISO-8601 formatted date-time string representing the time stamp.
         */
        @JvmStatic
        fun toString(rawValue: Int): String {
            return TimeDateStamp32(rawValue.toUInt()).toString()
        }
    }
}
