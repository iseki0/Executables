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

@Serializable(with = PE32Magic.Serializer::class)
@JvmInline
value class PE32Magic(private val magic: Short) {
    init {
        require(isValidMagic(magic)) { "Invalid PE32Magic: $magic" }
    }

    object Serializer : KSerializer<PE32Magic> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): PE32Magic {
            try {
                return valueOf(decoder.decodeString())
            } catch (e: IllegalArgumentException) {
                throw SerializationException(e.message)
            }
        }

        override fun serialize(encoder: Encoder, value: PE32Magic) {
            encoder.encodeString(value.toString())
        }

    }

    companion object {
        object Constants {
            const val PE32: Short = 0x10b
            const val PE32Plus: Short = 0x20b
        }

        val PE32 = PE32Magic(Constants.PE32)
        val PE32Plus = PE32Magic(Constants.PE32Plus)

        @JvmStatic
        fun isValidMagic(m: Short): Boolean {
            return m == Constants.PE32 || m == Constants.PE32Plus
        }

        @JvmStatic
        fun toString(m: Short): String {
            return PE32Magic(m).toString()
        }

        @JvmStatic
        fun valueOf(magic: Short): PE32Magic {
            return PE32Magic(magic)
        }

        @JvmStatic
        fun valueOf(kind: String): PE32Magic {
            return when (kind) {
                "PE32" -> PE32
                "PE32+" -> PE32Plus
                else -> throw IllegalArgumentException("must be PE32 or PE32+")
            }
        }

    }

    override fun toString(): String {
        return when (magic) {
            Constants.PE32 -> "PE32"
            Constants.PE32Plus -> "PE32+"
            else -> error("Unknown PE32Magic: $magic")
        }
    }
}
