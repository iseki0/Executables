package space.iseki.executables.pe.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import space.iseki.executables.pe.PE32Magic

internal object PE32MagicSerializer : KSerializer<PE32Magic> {
    override val descriptor: SerialDescriptor = serializer<String>().descriptor

    override fun serialize(encoder: Encoder, value: PE32Magic) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): PE32Magic {
        return PE32Magic.valueOf(decoder.decodeString())
    }
} 