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
value class Elf64Half(val value: UShort)

@JvmInline
@Serializable
value class Elf64Word(val value: UInt)

@JvmInline
@Serializable(Elf64Addr.Serializer::class)
value class Elf64Addr(val value: ULong) {
    object Serializer : KSerializer<Elf64Addr> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): Elf64Addr {
            return Elf64Addr(decodeULongHex(decoder, "Elf64Addr"))
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun serialize(encoder: Encoder, value: Elf64Addr) {
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
value class Elf64Off(val value: ULong)

