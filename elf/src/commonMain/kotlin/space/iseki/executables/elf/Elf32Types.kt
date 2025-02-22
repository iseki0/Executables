package space.iseki.executables.elf

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Elf32Half(val value: UShort)

@JvmInline
@Serializable
value class Elf32Word(val value: UInt)

@JvmInline
@Serializable(Elf32Addr.Serializer::class)
value class Elf32Addr(val value: UInt) {
    object Serializer : KSerializer<Elf32Addr> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): Elf32Addr {
            return Elf32Addr(decodeUIntHex(decoder, "Elf32Addr"))
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun serialize(encoder: Encoder, value: Elf32Addr) {
            encoder.encodeString("0x${value.value.toHexString()}")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "0x${value.toHexString()}"
    }
}

@JvmInline
@Serializable
value class Elf32Off(val value: UInt)

