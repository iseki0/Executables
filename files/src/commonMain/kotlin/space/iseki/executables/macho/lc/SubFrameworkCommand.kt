package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoLoadCommandType
import space.iseki.executables.share.u4

/**
 * Represents a LC_SUB_FRAMEWORK load command in a Mach-O file.
 *
 * This command specifies the name of the umbrella framework of which the current
 * binary is a subframework.
 *
 * Corresponds to the native C structure `struct sub_framework_command`.
 */
data class SubFrameworkCommand internal constructor(
    /**
     * The name of the umbrella framework (e.g., "AppKit").
     */
    val umbrella: String,

    /**
     * Actual size of this command, as read from the file.
     */
    override val size: UInt,

    /**
     * Always LC_SUB_FRAMEWORK.
     */
    override val type: MachoLoadCommandType,
) : MachoLoadCommand {

    companion object {
        private const val MIN_SIZE = 12u

        /**
         * Parses a [SubFrameworkCommand] from the buffer.
         *
         * @param buf The raw Mach-O file content.
         * @param off Offset to the start of this load command.
         * @param le Whether little-endian.
         * @throws InvalidLoaderCommandException if structure is invalid.
         */
        internal fun parse(
            buf: ByteArray,
            off: Int,
            le: Boolean,
        ): SubFrameworkCommand {
            val type = MachoLoadCommandType.LC_SUB_FRAMEWORK
            val cmdsize = buf.u4(off + 4, le)
            if (cmdsize < MIN_SIZE) {
                throw InvalidLoaderCommandException(
                    cmd = type,
                    message = "Invalid sub_framework_command: cmdsize ($cmdsize) < minimum expected ($MIN_SIZE)"
                )
            }

            val umbrellaOffset = buf.u4(off + 8, le)
            val umbrella = LCStr.readString(buf, off, umbrellaOffset)

            return SubFrameworkCommand(
                umbrella = umbrella,
                size = cmdsize,
                type = type
            )
        }
    }
}