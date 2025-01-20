package space.iseki.executables.pe

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@Serializable(with = VersionInfoFileOS.Serializer::class)
@JvmInline
value class VersionInfoFileOS(private val os: UInt) {
    object Serializer : KSerializer<VersionInfoFileOS> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): VersionInfoFileOS {
            try {
                return valueOf(decoder.decodeString())
            } catch (e: IllegalArgumentException) {
                throw SerializationException(e.message)
            }
        }

        override fun serialize(encoder: Encoder, value: VersionInfoFileOS) {
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

        val UNKNOWN = VersionInfoFileOS(Constants.UNKNOWN)
        val DOS = VersionInfoFileOS(Constants.DOS)
        val OS216 = VersionInfoFileOS(Constants.OS216)
        val OS232 = VersionInfoFileOS(Constants.OS232)
        val NT = VersionInfoFileOS(Constants.NT)
        val WINCE = VersionInfoFileOS(Constants.WINCE)

        fun valueOf(name: String): VersionInfoFileOS {
            return when (name) {
                "UNKNOWN" -> UNKNOWN
                "DOS" -> DOS
                "OS2_16" -> OS216
                "OS2_32" -> OS232
                "NT" -> NT
                "WINCE" -> WINCE
                else -> {
                    try {
                        VersionInfoFileOS(name.removePrefix("0x").toUInt(16))
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