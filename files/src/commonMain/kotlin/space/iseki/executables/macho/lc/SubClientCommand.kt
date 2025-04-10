package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoLoadCommandType
import space.iseki.executables.share.u4

/**
 * Represents a LC_SUB_CLIENT load command in a Mach-O file.
 *
 * This command marks a specific client as authorized to link to this subframework
 * directly instead of going through the umbrella.
 */
data class SubClientCommand internal constructor(
    val client: String,
    override val size: UInt,
    override val type: MachoLoadCommandType,
) : MachoLoadCommand {
    companion object {
        private const val MIN_SIZE = 12u

        internal fun parse(buf: ByteArray, off: Int, le: Boolean): SubClientCommand {
            val type = MachoLoadCommandType.LC_SUB_CLIENT
            val cmdsize = buf.u4(off + 4, le)
            if (cmdsize < MIN_SIZE) {
                throw InvalidLoaderCommandException(
                    cmd = type,
                    message = "Invalid sub_client_command: cmdsize ($cmdsize) < minimum expected ($MIN_SIZE)"
                )
            }

            val offset = buf.u4(off + 8, le)
            val name = LCStr.readString(buf, off, offset)

            return SubClientCommand(name, cmdsize, type)
        }
    }
}

