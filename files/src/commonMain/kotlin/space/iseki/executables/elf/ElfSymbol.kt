package space.iseki.executables.elf

import space.iseki.executables.share.u1
import space.iseki.executables.share.u2
import space.iseki.executables.share.u4
import space.iseki.executables.share.u8

/**
 * Base interface for ELF symbol table entries
 */
internal sealed interface ElfSym {
    /**
     * Symbol name index in string table
     *
     * This is an index into the symbol string table, giving the location of the symbol's name.
     * If the value is zero, the symbol has no name.
     */
    val stName: UInt

    /**
     * Symbol value
     *
     * This gives the value of the associated symbol. Depending on the context, this may be
     * an absolute value, an address, etc. For defined symbols, this is typically the memory
     * address of the symbol. For undefined symbols, this is typically zero.
     */
    val stValue: ULong

    /**
     * Symbol size
     *
     * This member gives the size of the symbol. For example, a data object's size is the
     * number of bytes contained in the object. This member holds zero if the symbol has no size
     * or an unknown size.
     */
    val stSize: ULong

    /**
     * Symbol type and binding
     *
     * This member specifies the symbol's type and binding attributes. The binding attributes
     * determine the symbol's visibility and behavior during linking. The type attributes
     * specify whether the associated symbol is a data object, function, section, or file.
     *
     * The high 4 bits specify the binding (STB_*), and the low 4 bits specify the type (STT_*).
     */
    val stInfo: UByte

    /**
     * Symbol visibility and reserved bits
     *
     * This member currently specifies a symbol's visibility, which defines how a symbol may be
     * accessed once it becomes part of an executable or shared object. The low 2 bits specify
     * the visibility (STV_*), and the remaining bits are reserved.
     */
    val stOther: UByte

    /**
     * Section index
     *
     * This member gives the section header table index in which the symbol is defined.
     * Some section indices have special meanings:
     * - SHN_UNDEF (0): The symbol is undefined and must be resolved at link time.
     * - SHN_ABS: The symbol has an absolute value that will not change during linking.
     * - SHN_COMMON: The symbol labels a common block that has not yet been allocated.
     */
    val stShndx: UShort

    /**
     * Get the binding attribute from the st_info field
     */
    fun getBinding(): ElfSymBinding {
        val bindingValue = (stInfo.toUInt() shr 4).toUByte()
        return ElfSymBinding(bindingValue)
    }

    /**
     * Get the type attribute from the st_info field
     */
    fun getType(): ElfSymType {
        val typeValue = (stInfo.toUInt() and 0xFu).toUByte()
        return ElfSymType(typeValue)
    }

    /**
     * Get the visibility attribute from the st_other field
     */
    fun getVisibility(): ElfSymVisibility {
        val visibilityValue = (stOther.toUInt() and 0x3u).toUByte()
        return ElfSymVisibility(visibilityValue)
    }

    companion object {
        /**
         * Create an st_info value from binding and type
         */
        fun makeInfo(binding: UByte, type: UByte): UByte {
            return ((binding.toUInt() shl 4) or (type.toUInt() and 0xFu)).toUByte()
        }
    }
}

/**
 * 32-bit ELF symbol table entry
 */
internal data class Elf32Sym(
    override val stName: UInt,
    val stValue32: UInt,
    val stSize32: UInt,
    override val stInfo: UByte,
    override val stOther: UByte,
    override val stShndx: UShort,
) : ElfSym {
    override val stValue: ULong
        get() = stValue32.toULong()
    override val stSize: ULong
        get() = stSize32.toULong()

    companion object {
        const val SIZE = 16

        fun parse(bytes: ByteArray, offset: Int, le: Boolean): Elf32Sym {
            val stName = bytes.u4(offset + 0, le)
            val stValue = bytes.u4(offset + 4, le)
            val stSize = bytes.u4(offset + 8, le)
            val stInfo = bytes.u1(offset + 12)
            val stOther = bytes.u1(offset + 13)
            val stShndx = bytes.u2(offset + 14, le)

            return Elf32Sym(
                stName = stName,
                stValue32 = stValue,
                stSize32 = stSize,
                stInfo = stInfo,
                stOther = stOther,
                stShndx = stShndx
            )
        }
    }
}

/**
 * 64-bit ELF symbol table entry
 */
internal data class Elf64Sym(
    override val stName: UInt,
    override val stInfo: UByte,
    override val stOther: UByte,
    override val stShndx: UShort,
    override val stValue: ULong,
    override val stSize: ULong,
) : ElfSym {
    companion object {
        const val SIZE = 24

        fun parse(bytes: ByteArray, offset: Int, le: Boolean): Elf64Sym {
            val stName = bytes.u4(offset + 0, le)
            val stInfo = bytes.u1(offset + 4)
            val stOther = bytes.u1(offset + 5)
            val stShndx = bytes.u2(offset + 6, le)
            val stValue = bytes.u8(offset + 8, le)
            val stSize = bytes.u8(offset + 16, le)

            return Elf64Sym(
                stName = stName,
                stInfo = stInfo,
                stOther = stOther,
                stShndx = stShndx,
                stValue = stValue,
                stSize = stSize
            )
        }
    }
}

