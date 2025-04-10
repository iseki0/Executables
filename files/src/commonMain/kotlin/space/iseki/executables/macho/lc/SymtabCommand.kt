package space.iseki.executables.macho.lc

//import space.iseki.executables.macho.MachoLoadCommandType
//import space.iseki.executables.macho.MachoSymbolDescFlags
//import space.iseki.executables.share.u1
//import space.iseki.executables.share.u2
//import space.iseki.executables.share.u4
//import space.iseki.executables.share.u8
//
///**
// * Represents an LC_SYMTAB load command, which provides offsets and sizes for
// * the symbol table and string table in the Mach-O file.
// */
//data class SymtabCommand internal constructor(
//    val symoff: UInt,
//    val nsyms: UInt,
//    val stroff: UInt,
//    val strsize: UInt,
//    override val size: UInt,
//    override val type: MachoLoadCommandType,
//) : MachoLoadCommand {
//
//    companion object {
//        private const val MIN_SIZE = 24u
//
//        /**
//         * Parses an [SymtabCommand] from the given buffer.
//         *
//         * @param buf The Mach-O file content.
//         * @param off Offset to the start of the load command.
//         * @param le Whether little-endian.
//         * @throws InvalidLoaderCommandException if the command is malformed.
//         */
//        internal fun parse(buf: ByteArray, off: Int, le: Boolean): SymtabCommand {
//            val type = MachoLoadCommandType.LC_SYMTAB
//            val cmdsize = buf.u4(off + 4, le)
//            if (cmdsize < MIN_SIZE) {
//                throw InvalidLoaderCommandException(
//                    cmd = type, message = "Invalid symtab_command: cmdsize ($cmdsize) < expected ($MIN_SIZE)"
//                )
//            }
//
//            val symoff = buf.u4(off + 8, le)
//            val nsyms = buf.u4(off + 12, le)
//            val stroff = buf.u4(off + 16, le)
//            val strsize = buf.u4(off + 20, le)
//
//            return SymtabCommand(symoff, nsyms, stroff, strsize, cmdsize, type)
//        }
//    }
//}
//
///**
// * Represents a single entry in the 32-bit symbol table (nlist structure).
// */
//data class SymbolEntry(
//    /**
//     * Index into the string table. 0 indicates an empty string.
//     */
//    val strx: UInt,
//
//    /**
//     * Type of symbol (see N_TYPE and friends).
//     */
//    val type: UByte,
//
//    /**
//     * Index of section this symbol is associated with (1-based).
//     */
//    val sect: UByte,
//
//    /**
//     * Packed flags for symbol visibility, scope, etc.
//     */
//    val desc: MachoSymbolDescFlags,
//
//    /**
//     * Symbol value: address, offset, or absolute value.
//     */
//    val value: UInt,
//) {
//    companion object {
//        private const val ENTRY_SIZE = 12
//
//        /**
//         * Parses a 32-bit symbol table entry (nlist) at the given offset.
//         */
//        internal fun parse(buf: ByteArray, off: Int, le: Boolean): SymbolEntry {
//            val strx = buf.u4(off, le)
//            val type = buf.u1(off + 4)
//            val sect = buf.u1(off + 5)
//            val desc = MachoSymbolDescFlags(buf.u2(off + 6, le))
//            val value = buf.u4(off + 8, le)
//
//            return SymbolEntry(strx, type, sect, desc, value)
//        }
//
//        /**
//         * Size of each symbol table entry (nlist) in bytes.
//         */
//        val SIZE = ENTRY_SIZE
//    }
//}
//
///**
// * Represents a single entry in the 64-bit symbol table (nlist_64 structure).
// */
//data class SymbolEntry64(
//    /**
//     * Index into the string table. 0 indicates an empty string.
//     */
//    val strx: UInt,
//
//    /**
//     * Type of symbol (see N_TYPE, N_SECT, etc).
//     */
//    val type: UByte,
//
//    /**
//     * Index of the associated section (1-based).
//     */
//    val sect: UByte,
//
//    /**
//     * Packed symbol descriptor flags.
//     */
//    val desc: MachoSymbolDescFlags,
//
//    /**
//     * The symbol’s value: address, offset, or absolute constant.
//     */
//    val value: ULong,
//) {
//    companion object {
//        private const val ENTRY_SIZE = 16
//
//        /**
//         * Parses a 64-bit symbol table entry (nlist_64) at the given offset.
//         *
//         * @param buf ByteArray containing the file.
//         * @param off Offset to the symbol entry.
//         * @param le Whether the data is little-endian.
//         */
//        internal fun parse(buf: ByteArray, off: Int, le: Boolean): SymbolEntry64 {
//            val strx = buf.u4(off, le)
//            val type = buf.u1(off + 4)
//            val sect = buf.u1(off + 5)
//            val desc = MachoSymbolDescFlags(buf.u2(off + 6, le))
//            val value = buf.u8(off + 8, le)
//
//            return SymbolEntry64(strx, type, sect, desc, value)
//        }
//
//        /**
//         * Size of each nlist_64 entry in bytes.
//         */
//        val SIZE = ENTRY_SIZE
//    }
//}
