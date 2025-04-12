package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoLoadCommandType
import kotlin.uuid.ExperimentalUuidApi

/**
 * Specifies the 128-bit UUID for an image or its corresponding dSYM file.
 */
@OptIn(ExperimentalUuidApi::class)
data class UuidCommand internal constructor(private val value: kotlin.uuid.Uuid) : MachoLoadCommand {
    override val size: UInt
        get() = 24u
    override val type: MachoLoadCommandType
        get() = MachoLoadCommandType.LC_UUID

    fun kotlinUuid(): kotlin.uuid.Uuid = value

    companion object {
        internal fun parse(buf: ByteArray, off: Int, le: Boolean): UuidCommand =
            UuidCommand(kotlin.uuid.Uuid.fromByteArray(buf.sliceArray(off + 8 until off + 24)))
    }
}

