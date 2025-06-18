package space.iseki.executables.elf

import space.iseki.executables.share.u1
import space.iseki.executables.share.u2
import space.iseki.executables.share.u4
import space.iseki.executables.share.u8


internal data class ElfSym(
    /**
     * Symbol name index in string table
     *
     * This is an index into the symbol string table, giving the location of the symbol's name.
     * If the value is zero, the symbol has no name.
     */
    val stName: UInt,
    /**
     * Symbol type and binding
     *
     * This member specifies the symbol's type and binding attributes. The binding attributes
     * determine the symbol's visibility and behavior during linking. The type attributes
     * specify whether the associated symbol is a data object, function, section, or file.
     *
     * The high 4 bits specify the binding (STB_*), and the low 4 bits specify the type (STT_*).
     */
    val stInfo: UByte,
    /**
     * Symbol visibility and reserved bits
     *
     * This member currently specifies a symbol's visibility, which defines how a symbol may be
     * accessed once it becomes part of an executable or shared object. The low 2 bits specify
     * the visibility (STV_*), and the remaining bits are reserved.
     */
    val stOther: UByte,
    /**
     * Section index
     *
     * This member gives the section header table index in which the symbol is defined.
     * Some section indices have special meanings:
     * - SHN_UNDEF (0): The symbol is undefined and must be resolved at link time.
     * - SHN_ABS: The symbol has an absolute value that will not change during linking.
     * - SHN_COMMON: The symbol labels a common block that has not yet been allocated.
     */
    val stShndx: UShort,
    /**
     * Symbol value
     *
     * This gives the value of the associated symbol. Depending on the context, this may be
     * an absolute value, an address, etc. For defined symbols, this is typically the memory
     * address of the symbol. For undefined symbols, this is typically zero.
     */
    val stValue: ULong,
    /**
     * Symbol size
     *
     * This member gives the size of the symbol. For example, a data object's size is the
     * number of bytes contained in the object. This member holds zero if the symbol has no size
     * or an unknown size.
     */
    val stSize: ULong,
) {

    /**
     * Get the binding attribute from the st_info field
     */
    val binding
        get() = ElfSymBinding((stInfo.toUInt() shr 4).toUByte())

    /**
     * Get the type attribute from the st_info field
     */
    val type
        get() = (stInfo.toUInt() and 0xFu).toUByte().let(::ElfSymType)


    /**
     * Get the visibility attribute from the st_other field
     */
    val visibility
        get() = (stOther.toUInt() and 0x3u).toUByte().let(::ElfSymVisibility)


    companion object {
        internal const val SIZE_32 = 16
        internal const val SIZE_64 = 24

        internal fun parse(bytes: ByteArray, offset: Int, le: Boolean, is64Bit: Boolean): ElfSym =
            if (is64Bit) parse64(bytes, offset, le) else parse32(bytes, offset, le)

        private fun parse32(bytes: ByteArray, offset: Int, le: Boolean): ElfSym {
            val stName = bytes.u4(offset + 0, le)
            val stValue = bytes.u4(offset + 4, le)
            val stSize = bytes.u4(offset + 8, le)
            val stInfo = bytes.u1(offset + 12)
            val stOther = bytes.u1(offset + 13)
            val stShndx = bytes.u2(offset + 14, le)

            return ElfSym(
                stName = stName,
                stValue = stValue.toULong(),
                stSize = stSize.toULong(),
                stInfo = stInfo,
                stOther = stOther,
                stShndx = stShndx,
            )
        }

        private fun parse64(bytes: ByteArray, offset: Int, le: Boolean): ElfSym {
            val stName = bytes.u4(offset + 0, le)
            val stInfo = bytes.u1(offset + 4)
            val stOther = bytes.u1(offset + 5)
            val stShndx = bytes.u2(offset + 6, le)
            val stValue = bytes.u8(offset + 8, le)
            val stSize = bytes.u8(offset + 16, le)

            return ElfSym(
                stName = stName,
                stInfo = stInfo,
                stOther = stOther,
                stShndx = stShndx,
                stValue = stValue,
                stSize = stSize,
            )
        }

    }
}

