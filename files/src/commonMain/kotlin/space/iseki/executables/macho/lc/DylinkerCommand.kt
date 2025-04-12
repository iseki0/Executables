package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoLoadCommandType
import space.iseki.executables.share.u4

/**
 * Represents a LC_LOAD_DYLINKER or LC_ID_DYLINKER load command.
 *
 * Declares the path to the dynamic linker used to launch dynamically linked executables.
 *
 * Corresponds to the native C structure:
 * ```c
 * struct dylinker_command {
 *     uint32_t cmd;
 *     uint32_t cmdsize;
 *     union lc_str name;
 * };
 * ```
 */
data class DylinkerCommand internal constructor(
    /**
     * The dynamic linker path (e.g., `/usr/lib/dyld`).
     */
    val name: String,

    /**
     * Actual size of the command, read from file.
     */
    override val size: UInt,

    /**
     * Command type: LC_LOAD_DYLINKER or LC_ID_DYLINKER.
     */
    override val type: MachoLoadCommandType,
) : MachoLoadCommand {

    companion object {
        private const val MIN_SIZE = 12u

        /**
         * Parses a [DylinkerCommand] from the given buffer.
         *
         * @param buf Mach-O file content.
         * @param off Offset where this command starts.
         * @param le Whether little-endian.
         * @param type Command type (LC_LOAD_DYLINKER or LC_ID_DYLINKER).
         * @throws InvalidLoaderCommandException if the structure is invalid.
         */
        internal fun parse(
            buf: ByteArray,
            off: Int,
            le: Boolean,
            type: MachoLoadCommandType,
        ): DylinkerCommand {
            val cmdsize = buf.u4(off + 4, le)
            if (cmdsize < MIN_SIZE) {
                throw InvalidLoaderCommandException(
                    cmd = type,
                    message = "Invalid dylinker_command: cmdsize ($cmdsize) < minimum expected ($MIN_SIZE)"
                )
            }

            val nameOffset = buf.u4(off + 8, le)
            val name = LCStr.readString(buf, off, nameOffset)

            return DylinkerCommand(
                name = name,
                size = cmdsize,
                type = type
            )
        }
    }
}


