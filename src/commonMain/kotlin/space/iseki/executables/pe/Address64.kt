package space.iseki.executables.pe

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
value class Address64(val rawValue: Long) : Comparable<Address64> {
    override fun compareTo(other: Address64): Int {
        return rawValue.toULong().compareTo(other.rawValue.toULong())
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "0x${rawValue.toHexString()}"
    }

    companion object {
        @JvmStatic
        fun toString(rawValue: Long): String {
            return Address64(rawValue).toString()
        }
    }
}