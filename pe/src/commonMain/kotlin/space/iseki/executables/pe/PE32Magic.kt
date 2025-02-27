package space.iseki.executables.pe

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

/**
 * Represents the pe32 magic value in a pe file.
 *
 * @property value the raw short value representing the magic
 */
@JvmInline
@Serializable(with = PE32Magic.Serializer::class)
value class PE32Magic(val value: Short) : Comparable<PE32Magic> {
    internal object Serializer : KSerializer<PE32Magic> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): PE32Magic {
            return valueOf(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: PE32Magic) {
            encoder.encodeString(value.toString())
        }

    }

    companion object {
        /**
         * Returns the [PE32Magic] corresponding to the given name.
         *
         * @param name the name of the magic, either "PE32" or "PE32+"
         * @return a [PE32Magic] instance
         * @throws IllegalArgumentException if the name is unknown
         */
        @JvmStatic
        fun valueOf(name: String): PE32Magic = when (name) {
            "PE32" -> PE32
            "PE32+" -> PE32Plus
            else -> throw IllegalArgumentException("Unknown PE32Magic: $name")
        }

        /**
         * The pe32 magic value.
         */
        val PE32 = PE32Magic(0x10b)

        /**
         * The pe32+ magic value.
         */
        val PE32Plus = PE32Magic(0x20b)
    }

    override fun compareTo(other: PE32Magic): Int = value.compareTo(other.value)

    override fun toString(): String = when (this) {
        PE32 -> "PE32"
        PE32Plus -> "PE32+"
        else -> "UNKNOWN($value)"
    }
}
