package space.iseki.executables.share

import space.iseki.executables.common.DataAccessor
import kotlin.math.min

internal class MemReader(val dataAccessor: DataAccessor) {
    data class Segment(
        val vOff: ULong,
        val fOff: ULong,
        val fSize: ULong,
    )

    private val m = mutableListOf<Segment>()
    fun mapMemory(vOff: ULong, fOff: ULong, fSize: ULong) {
        if (vOff == 0uL && fOff == 0uL && fSize == 0uL) {
            return
        }
        // check if the segment is already mapped
        val last = m.lastOrNull()
        if (last != null) {
            check(last.vOff <= vOff) { "the new section if before last one: $last, $vOff, $fOff, $fSize" }
            check(last.vOff + last.fSize <= vOff) { "overlapping segments: $last, $vOff, $fOff, $fSize" }
        }
        m.add(Segment(vOff, fOff, fSize))
    }

    fun read(
        pos: ULong,
        buf: ByteArray,
        off: Int,
        len: Int,
    ) {
        // off0 is the offset in the buffer
        var off0 = off
        // pos0 is the position in the memory
        var pos0 = pos

        val endVAddr = pos + len.toULong()
        for (segment in m) {
            val vOff = segment.vOff
            val fOff = segment.fOff
            val fSize = segment.fSize
            // skip if the segment is before the pos totally
            if (vOff + fSize < pos0) continue
            // no more segments to read, the current one is out of range
            if (vOff >= endVAddr) break
            // fill the gap with zero
            if (vOff > pos0) {
                buf.fill(0, off0, off0 + (vOff - pos0).toInt())
                off0 += (vOff - pos0).toInt()
                pos0 = vOff
            }
            // vSkip is the bytes we should skip in the memory
            val vSkip = pos0 - vOff
            pos0 += vSkip
            check(fOff + vSkip <= Long.MAX_VALUE.toULong())
            val bytesWeShouldRead = min(fSize - vSkip, (len - off0).toULong()).toInt()
            check(bytesWeShouldRead >= 0)
            if (bytesWeShouldRead > 0) dataAccessor.readFully((fOff + vSkip).toLong(), buf, off0, bytesWeShouldRead)
            off0 += bytesWeShouldRead
            pos0 += bytesWeShouldRead.toULong()
        }

        // fill the rest with zero
        if (off0 < off + len) {
            buf.fill(0, off0, off + len)
        }

    }
}

