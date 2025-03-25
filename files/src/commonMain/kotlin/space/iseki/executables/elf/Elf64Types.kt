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
value class Elf64Half(val value: UShort) : ElfPrimitive, Comparable<Elf64Half> {
    override fun compareTo(other: Elf64Half): Int = value.compareTo(other.value)
}

/**
 * Represents a 32-bit unsigned integer in ELF format.
 * Equivalent to uint32_t in C.
 */
@JvmInline
@Serializable
value class Elf64Word(val value: UInt) : ElfPrimitive, Comparable<Elf64Word> {
    override fun compareTo(other: Elf64Word): Int = value.compareTo(other.value)
}

/**
 * Represents a 64-bit unsigned integer in ELF format.
 * Equivalent to uint64_t in C.
 */
@JvmInline
@Serializable
value class Elf64Xword(val value: ULong) : ElfPrimitive, Comparable<Elf64Xword> {
    override fun compareTo(other: Elf64Xword): Int = value.compareTo(other.value)
}

/**
 * Represents a 64-bit signed integer in ELF format.
 * Equivalent to int64_t in C.
 */
@JvmInline
@Serializable
value class Elf64Sxword(val value: Long) : ElfPrimitive, Comparable<Elf64Sxword> {
    override fun compareTo(other: Elf64Sxword): Int = value.compareTo(other.value)
}

/**
 * Represents an unsigned program address in 64-bit ELF format.
 * Equivalent to uint64_t in C.
 */
@JvmInline
@Serializable(Elf64Addr.Serializer::class)
value class Elf64Addr(val value: ULong) : ElfPrimitive, Comparable<Elf64Addr> {
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

    override fun compareTo(other: Elf64Addr): Int = value.compareTo(other.value)

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
value class Elf64Off(val value: ULong) : ElfPrimitive, Comparable<Elf64Off> {
    override fun compareTo(other: Elf64Off): Int = value.compareTo(other.value)
}

