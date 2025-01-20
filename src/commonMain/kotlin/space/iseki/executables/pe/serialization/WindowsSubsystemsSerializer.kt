package space.iseki.executables.pe.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import space.iseki.executables.pe.WindowsSubsystems

internal object WindowsSubsystemsSerializer : KSerializer<WindowsSubsystems> {
    override val descriptor: SerialDescriptor = serializer<String>().descriptor

    override fun serialize(encoder: Encoder, value: WindowsSubsystems) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): WindowsSubsystems {
        return WindowsSubsystems.valueOf(decoder.decodeString())
    }
} 