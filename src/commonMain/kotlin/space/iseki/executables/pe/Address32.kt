package space.iseki.executables.pe

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
value class Address32(val rawValue: Int) : Comparable<Address32> {
    override fun compareTo(other: Address32): Int {
        return rawValue.toUInt().compareTo(other.rawValue.toUInt())
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "0x${rawValue.toHexString()}"
    }

    companion object {
        @JvmStatic
        fun toString(rawValue: Int): String {
            return Address32(rawValue).toString()
        }
    }
}
