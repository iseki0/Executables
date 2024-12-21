package space.iseki.executables.pe

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
value class DataDirectoryItem(
    private val rawValue: Long,
) {
    constructor(virtualAddress: Int, size: Int) : this((virtualAddress.toLong() shl 32) or size.toLong())
    companion object {
        val ZERO = DataDirectoryItem(0)

        operator fun invoke(virtualAddress: UInt, size: UInt): DataDirectoryItem {
            return DataDirectoryItem(virtualAddress.toInt(), size.toInt())
        }

        @JvmStatic
        fun toString(rawValue: Long): String {
            return DataDirectoryItem(rawValue).toString()
        }
    }

    val virtualAddress: Int
        get() = (rawValue ushr 32).toInt()

    val size: Int
        get() = rawValue.toInt()

    override fun toString(): String {
        if (this == ZERO) {
            return "ZERO"
        }
        return "[${Address32(virtualAddress)} + $size]"
    }
}

