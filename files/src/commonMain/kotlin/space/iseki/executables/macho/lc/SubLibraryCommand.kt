package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoLoadCommandType
import space.iseki.executables.share.u4

/**
 * Represents a LC_SUB_LIBRARY load command in a Mach-O file.
 *
 * This command marks the given sublibrary as part of an umbrella framework.
 * Clients may link to sublibraries directly.
 */
data class SubLibraryCommand internal constructor(
    val subLibrary: String,
    override val size: UInt,
    override val type: MachoLoadCommandType,
) : MachoLoadCommand {
    companion object {
        private const val MIN_SIZE = 12u

        internal fun parse(buf: ByteArray, off: Int, le: Boolean): SubLibraryCommand {
            val type = MachoLoadCommandType.LC_SUB_LIBRARY
            val cmdsize = buf.u4(off + 4, le)
            if (cmdsize < MIN_SIZE) {
                throw InvalidLoaderCommandException(
                    cmd = type,
                    message = "Invalid sub_library_command: cmdsize ($cmdsize) < minimum expected ($MIN_SIZE)"
                )
            }

            val offset = buf.u4(off + 8, le)
            val name = LCStr.readString(buf, off, offset)

            return SubLibraryCommand(name, cmdsize, type)
        }
    }
}