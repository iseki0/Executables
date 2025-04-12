package space.iseki.executables.macho

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(with = PackedVersion.Serializer::class)
value class PackedVersion(val value: UInt) : Comparable<PackedVersion> {

    constructor(major: UInt, minor: UInt, subminor: UInt) : this(
        (major shl 16) or ((minor and 0xFFu) shl 8) or (subminor and 0xFFu)
    )

    val major: UInt get() = (value shr 16) and 0xFFFFu
    val minor: UInt get() = (value shr 8) and 0xFFu
    val subminor: UInt get() = value and 0xFFu

    override fun compareTo(other: PackedVersion): Int {
        return value.compareTo(other.value)
    }

    override fun toString(): String {
        return "$major.$minor.$subminor"
    }

    object Serializer : KSerializer<PackedVersion> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PackedVersion", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: PackedVersion) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): PackedVersion {
            val parts = decoder.decodeString().split('.')
            if (parts.size != 3) throw SerializationException("Invalid version format, expected 3 parts")
            try {
                val major = parts[0].toUInt()
                val minor = parts[1].toUInt()
                val subminor = parts[2].toUInt()
                if (major > 0xFFFFu || minor > 0xFFu || subminor > 0xFFu) {
                    throw SerializationException("Version parts out of range: major must be <= 65535, minor and subminor must be <= 255")
                }
                return PackedVersion(major, minor, subminor)
            } catch (e: NumberFormatException) {
                throw SerializationException("Invalid version format: ${e.message}", e)
            }
        }
    }
}
