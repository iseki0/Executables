package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoLoadCommandType
import space.iseki.executables.share.u4

/**
 * Represents a LC_TWOLEVEL_HINTS load command in a Mach-O binary.
 *
 * Corresponds to the native C structure `struct twolevel_hints_command`.
 *
 * This load command provides information about the location and count of
 * two-level namespace hint entries used for symbol lookup optimization.
 */
data class TwoLevelHintsCommand internal constructor(
    /**
     * The byte offset in the file to the start of the twolevel_hint array.
     */
    val offset: UInt,

    /**
     * The number of entries in the twolevel_hint table located at [offset].
     */
    val nhints: UInt,
) : MachoLoadCommand {

    /**
     * The total size of this command. Always 16 bytes for this structure.
     */
    override val size: UInt
        get() = 16u

    /**
     * The load command type (LC_TWOLEVEL_HINTS).
     */
    override val type: MachoLoadCommandType
        get() = MachoLoadCommandType.LC_TWOLEVEL_HINTS

    companion object {
        /**
         * Parses a [TwoLevelHintsCommand] from the given ByteArray at [off].
         *
         * @param buf The binary content of the Mach-O file.
         * @param off The byte offset in [buf] where the command begins.
         * @param le Whether the binary is little-endian.
         * @return A parsed [TwoLevelHintsCommand] instance.
         */
        internal fun parse(buf: ByteArray, off: Int, le: Boolean): TwoLevelHintsCommand {
            val offsetVal = buf.u4(off + 8, le)
            val nhints = buf.u4(off + 12, le)

            return TwoLevelHintsCommand(offsetVal, nhints)
        }
    }
}

