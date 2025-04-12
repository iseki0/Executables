package space.iseki.executables.macho.lc

import space.iseki.executables.common.TimeDateStamp32
import space.iseki.executables.macho.MachoLoadCommandType
import space.iseki.executables.macho.PackedVersion
import space.iseki.executables.share.u4

/**
 * Represents a LC_LOAD_DYLIB, LC_ID_DYLIB, or LC_LOAD_WEAK_DYLIB load command.
 *
 * Corresponds to the native C structure `struct dylib_command`, which includes the
 * `struct dylib` substructure holding versioning info and an offset to the dylib path string.
 */
data class DylibCommand internal constructor(
    /**
     * Path to the shared library. A null-terminated ASCII string inside this load command.
     */
    val name: String,

    /**
     * Timestamp used by the dynamic linker to evaluate prebinding.
     */
    val timestamp: TimeDateStamp32,

    /**
     * Current version of the dylib (packed: major << 16 | minor << 8 | patch).
     */
    val currentVersion: PackedVersion,

    /**
     * Minimum compatible version of the dylib (same packing format).
     */
    val compatibilityVersion: PackedVersion,

    /**
     * Actual size of this load command, read directly from the Mach-O file.
     */
    override val size: UInt,

    /**
     * Type of the load command: LC_LOAD_DYLIB, LC_ID_DYLIB, LC_LOAD_WEAK_DYLIB, etc.
     */
    override val type: MachoLoadCommandType,
) : MachoLoadCommand {

    companion object {
        /**
         * Parses a [DylibCommand] from the given buffer.
         *
         * @param buf The Mach-O file's raw content.
         * @param off Byte offset where this load command begins.
         * @param le Whether the Mach-O file is little-endian.
         * @param type One of the supported LC_*_DYLIB command types.
         * @return A parsed [DylibCommand] instance.
         */
        internal fun parse(
            buf: ByteArray,
            off: Int,
            le: Boolean,
            type: MachoLoadCommandType,
        ): DylibCommand {
            val cmdsize = buf.u4(off + 4, le)
            if (cmdsize < 24u) {
                throw InvalidLoaderCommandException(
                    cmd = type,
                    message = "Invalid dylib_command: cmdsize ($cmdsize) is smaller than minimum expected size (24 bytes)"
                )
            }

            val nameOffset = buf.u4(off + 8, le)
            val timestamp = buf.u4(off + 12, le)
            val currentVersion = buf.u4(off + 16, le)
            val compatibilityVersion = buf.u4(off + 20, le)

            // nameOffset is relative to the start of the load command (i.e., off)
            val name = LCStr.readString(buf, off, nameOffset)

            return DylibCommand(
                name = name,
                timestamp = TimeDateStamp32(timestamp),
                currentVersion = PackedVersion(currentVersion),
                compatibilityVersion = PackedVersion(compatibilityVersion),
                size = cmdsize,
                type = type
            )
        }
    }
}
