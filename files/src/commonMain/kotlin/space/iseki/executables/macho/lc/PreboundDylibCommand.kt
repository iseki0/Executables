package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoLoadCommandType
import space.iseki.executables.share.u4

/**
 * Represents an LC_PREBOUND_DYLIB load command in a Mach-O file.
 *
 * This command is used for prebound executables, indicating which modules
 * of a shared library were prebound at static link time.
 *
 * Corresponds to the native C structure `struct prebound_dylib_command`.
 */
data class PreboundDylibCommand internal constructor(
    /**
     * Name of the prebound shared library.
     */
    val name: String,

    /**
     * Number of modules in the shared library.
     */
    val nmodules: UInt,

    /**
     * A bitfield (as raw bytes) representing which modules are actually linked.
     */
    val linkedModules: ByteArray,

    /**
     * Size of this command, from the file.
     */
    override val size: UInt,

    /**
     * Always LC_PREBOUND_DYLIB.
     */
    override val type: MachoLoadCommandType,
) : MachoLoadCommand {

    companion object {
        private const val MIN_SIZE = 20u

        /**
         * Parses a [PreboundDylibCommand] from the buffer.
         *
         * @param buf Entire Mach-O file content.
         * @param off Offset to start of this load command.
         * @param le Whether the file is little-endian.
         * @throws InvalidLoaderCommandException if the structure is invalid.
         */
        internal fun parse(
            buf: ByteArray,
            off: Int,
            le: Boolean,
        ): PreboundDylibCommand {
            val type = MachoLoadCommandType.LC_PREBOUND_DYLIB
            val cmdsize = buf.u4(off + 4, le)
            if (cmdsize < MIN_SIZE) {
                throw InvalidLoaderCommandException(
                    cmd = type,
                    message = "Invalid prebound_dylib_command: cmdsize ($cmdsize) < minimum expected ($MIN_SIZE)"
                )
            }

            val nameOffset = buf.u4(off + 8, le)
            val nmodules = buf.u4(off + 12, le)
            val linkedModulesOffset = buf.u4(off + 16, le)

            val name = LCStr.readString(buf, off, nameOffset)

            // linkedModules is a variable-length bitfield of (nmodules + 7) / 8 bytes
            val bitCount = nmodules.toInt()
            val bitSize = (bitCount + 7) / 8
            val lmStart = off + linkedModulesOffset.toInt()
            val lmEnd = lmStart + bitSize
            if (lmEnd > off + cmdsize.toInt()) {
                throw InvalidLoaderCommandException(
                    cmd = type,
                    message = "linked_modules offset ($linkedModulesOffset) + size ($bitSize) exceeds command size ($cmdsize)"
                )
            }

            val linkedModules = buf.copyOfRange(lmStart, lmEnd)

            return PreboundDylibCommand(
                name = name,
                nmodules = nmodules,
                linkedModules = linkedModules,
                size = cmdsize,
                type = type
            )
        }
    }
}

