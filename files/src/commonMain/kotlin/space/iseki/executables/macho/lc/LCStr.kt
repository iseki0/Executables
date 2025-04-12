package space.iseki.executables.macho.lc

import space.iseki.executables.share.u4
import kotlin.jvm.JvmInline

/**
 * Represents a Mach-O `lc_str` structure, which holds an offset to a variable-length string
 * within a load command. Only the `offset` field is used in Mach-O files.
 *
 * Corresponds to:
 * ```c
 * union lc_str {
 *     uint32_t offset;
 * #ifndef __LP64__
 *     char* ptr;
 * #endif
 * };
 * ```
 */
@JvmInline
internal value class LCStr(val offset: UInt) {
    companion object {
        /**
         * Parses a [LCStr] from a 4-byte field at [off] in the buffer.
         *
         * @param buf ByteArray containing the Mach-O load command.
         * @param off Offset to the 4-byte lc_str.offset field.
         * @param le Whether the buffer is little-endian.
         */
        internal fun parse(buf: ByteArray, off: Int, le: Boolean): LCStr {
            return LCStr(buf.u4(off, le))
        }

        /**
         * Reads the actual string content from a given load command region,
         * based on [LCStr.offset].
         *
         * @param buf Entire buffer containing the load command and string.
         * @param cmdStart Offset to the start of the containing load command.
         * @param offsetFromCmd The offset field inside the LCStr (relative to command).
         * @return The decoded ASCII string, null-terminated and trimmed.
         */
        internal fun readString(buf: ByteArray, cmdStart: Int, offsetFromCmd: UInt): String {
            val strStart = cmdStart + offsetFromCmd.toInt()
            var p = strStart
            while (p < buf.size && buf[p] != 0.toByte()) {
                p++
            }
            val strEnd = p
            return buf.decodeToString(strStart, strEnd).trimEnd('\u0000')
        }
    }
}


