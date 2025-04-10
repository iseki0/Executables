package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoLoadCommandType
import space.iseki.executables.share.u4

/**
 * Represents a LC_SUB_UMBRELLA load command in a Mach-O file.
 *
 * This command marks the given framework name as a subumbrella of the current one.
 * Clients may link to a subumbrella directly.
 */
data class SubUmbrellaCommand internal constructor(
    val subUmbrella: String,
    override val size: UInt,
    override val type: MachoLoadCommandType,
) : MachoLoadCommand {
    companion object {
        private const val MIN_SIZE = 12u

        internal fun parse(buf: ByteArray, off: Int, le: Boolean): SubUmbrellaCommand {
            val type = MachoLoadCommandType.LC_SUB_UMBRELLA
            val cmdsize = buf.u4(off + 4, le)
            if (cmdsize < MIN_SIZE) {
                throw InvalidLoaderCommandException(
                    cmd = type,
                    message = "Invalid sub_umbrella_command: cmdsize ($cmdsize) < minimum expected ($MIN_SIZE)"
                )
            }

            val offset = buf.u4(off + 8, le)
            val name = LCStr.readString(buf, off, offset)

            return SubUmbrellaCommand(name, cmdsize, type)
        }
    }
}