package space.iseki.executables.elf

import space.iseki.executables.common.Address32
import space.iseki.executables.common.Address64

sealed interface ElfPrimitive {
    fun castToUByte(): UByte = castToULong().toUByte()
    fun castToUShort(): UShort = castToULong().toUShort()
    fun castToUInt(): UInt = castToULong().toUInt()
    fun castToULong(): ULong = when (this) {
        is Elf32Half -> value.toULong()
        is Elf32Word -> value.toULong()
        is Elf32Addr -> value.toULong()
        is Elf32Off -> value.toULong()
        is Elf32Sword -> value.toULong()
        is Elf64Half -> value.toULong()
        is Elf64Word -> value.toULong()
        is Elf64Xword -> value
        is Elf64Addr -> value
        is Elf64Off -> value
        is Elf64Sxword -> value.toULong()
    }

    fun castToByte(): Byte = castToULong().toByte()
    fun castToShort(): Short = castToULong().toShort()
    fun castToInt(): Int = castToULong().toInt()
    fun castToLong(): Long = castToULong().toLong()
    fun castToAddress64(): Address64 = Address64(castToULong())
    fun castToAddress32(): Address32 = Address32(castToUInt())
}
