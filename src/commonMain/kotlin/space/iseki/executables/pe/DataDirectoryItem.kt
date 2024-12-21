package space.iseki.executables.pe

import kotlin.jvm.JvmInline

@JvmInline
value class DataDirectoryItem(
    private val rawValue: Long,
) {
    val virtualAddress: Int
        get() = (rawValue ushr 32).toInt()

    val size: Int
        get() = rawValue.toInt()

    override fun toString(): String {
        return "(virtualAddress=$virtualAddress, size=$size)"
    }
}

