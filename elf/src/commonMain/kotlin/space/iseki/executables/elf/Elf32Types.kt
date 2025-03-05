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
value class Elf32Half(val value: UShort) : Primitive, Comparable<Elf32Half> {
    override fun compareTo(other: Elf32Half): Int = value.compareTo(other.value)
}

/**
 * Represents a 32-bit unsigned integer in ELF format.
 * Equivalent to uint32_t in C.
 */
@JvmInline
@Serializable
value class Elf32Word(val value: UInt) : Primitive, Comparable<Elf32Word> {
    override fun compareTo(other: Elf32Word): Int = value.compareTo(other.value)
}

/**
 * Represents a 32-bit signed integer in ELF format.
 * Equivalent to int32_t in C.
 */
@JvmInline
@Serializable
value class Elf32Sword(val value: Int) : Primitive, Comparable<Elf32Sword> {
    override fun compareTo(other: Elf32Sword): Int = value.compareTo(other.value)
}

/**
 * Represents an unsigned program address in 32-bit ELF format.
 * Equivalent to uint32_t in C.
 */
@JvmInline
@Serializable(Elf32Addr.Serializer::class)
value class Elf32Addr(val value: UInt) : Primitive, Comparable<Elf32Addr> {
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

    override fun compareTo(other: Elf32Addr): Int = value.compareTo(other.value)

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "0x${value.toHexString()}"
    }
}

/**
 * Represents an unsigned file offset in 32-bit ELF format.
 * Equivalent to uint32_t in C.
 */
@JvmInline
@Serializable
value class Elf32Off(val value: UInt) : Primitive, Comparable<Elf32Off> {
    override fun compareTo(other: Elf32Off): Int = value.compareTo(other.value)
}

