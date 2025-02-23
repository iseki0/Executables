package space.iseki.executables.elf

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

/**
 * Represents a 16-bit unsigned integer in ELF format.
 * Equivalent to uint16_t in C.
 */
@JvmInline
@Serializable
value class Elf64Half(val value: UShort) : Primitive

/**
 * Represents a 32-bit unsigned integer in ELF format.
 * Equivalent to uint32_t in C.
 */
@JvmInline
@Serializable
value class Elf64Word(val value: UInt) : Primitive

/**
 * Represents a 64-bit unsigned integer in ELF format.
 * Equivalent to uint64_t in C.
 */
@JvmInline
@Serializable
value class Elf64Xword(val value: ULong) : Primitive

/**
 * Represents a 64-bit signed integer in ELF format.
 * Equivalent to int64_t in C.
 */
@JvmInline
@Serializable
value class Elf64Sxword(val value: Long) : Primitive

/**
 * Represents an unsigned program address in 64-bit ELF format.
 * Equivalent to uint64_t in C.
 */
@JvmInline
@Serializable(Elf64Addr.Serializer::class)
value class Elf64Addr(val value: ULong) : Primitive {
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

/**
 * Represents an unsigned file offset in 64-bit ELF format.
 * Equivalent to uint64_t in C.
 */
@JvmInline
@Serializable
value class Elf64Off(val value: ULong) : Primitive

