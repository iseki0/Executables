package space.iseki.executables.pe

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@Serializable(with = FileOS.Serializer::class)
@JvmInline
value class FileOS(private val os: UInt) {
    object Serializer : KSerializer<FileOS> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): FileOS {
            try {
                return valueOf(decoder.decodeString())
            } catch (e: IllegalArgumentException) {
                throw SerializationException(e.message)
            }
        }

        override fun serialize(encoder: Encoder, value: FileOS) {
            encoder.encodeString(value.toString())
        }
    }

    companion object {
        object Constants {
            const val UNKNOWN = 0u
            const val DOS = 0x10000u
            const val OS216 = 0x20000u
            const val OS232 = 0x30000u
            const val NT = 0x40000u
            const val WINCE = 0x50000u
        }

        val UNKNOWN = FileOS(Constants.UNKNOWN)
        val DOS = FileOS(Constants.DOS)
        val OS216 = FileOS(Constants.OS216)
        val OS232 = FileOS(Constants.OS232)
        val NT = FileOS(Constants.NT)
        val WINCE = FileOS(Constants.WINCE)

        fun valueOf(name: String): FileOS {
            return when (name) {
                "UNKNOWN" -> UNKNOWN
                "DOS" -> DOS
                "OS2_16" -> OS216
                "OS2_32" -> OS232
                "NT" -> NT
                "WINCE" -> WINCE
                else -> {
                    try {
                        FileOS(name.removePrefix("0x").toUInt(16))
                    } catch (e: NumberFormatException) {
                        throw IllegalArgumentException("Unknown FileOS: $name")
                    }
                }
            }
        }
    }

    override fun toString(): String {
        return when (os) {
            Constants.UNKNOWN -> "UNKNOWN"
            Constants.DOS -> "DOS"
            Constants.OS216 -> "OS2_16"
            Constants.OS232 -> "OS2_32"
            Constants.NT -> "NT"
            Constants.WINCE -> "WINCE"
            else -> "0x${os.toString(16)}"
        }
    }
} 