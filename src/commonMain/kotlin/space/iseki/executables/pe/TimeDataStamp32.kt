package space.iseki.executables.pe

import kotlinx.datetime.Instant
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
value class TimeDataStamp32(val rawValue: Int) : Comparable<TimeDataStamp32> {
    override fun compareTo(other: TimeDataStamp32): Int {
        return rawValue.toUInt().compareTo(other.rawValue.toUInt())
    }

    override fun toString(): String {
        return Instant.fromEpochSeconds(rawValue.toUInt().toLong()).toString()
    }

    companion object {
        @JvmStatic
        fun toString(rawValue: Int): String {
            return TimeDataStamp32(rawValue).toString()
        }
    }
}
