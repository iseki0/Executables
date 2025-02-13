package space.iseki.executables.pe

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
value class PE32Magic(val rawValue: Short) {
    companion object {
        @JvmStatic
        fun valueOf(name: String): PE32Magic = when (name) {
            "PE32" -> PE32
            "PE32+" -> PE32Plus
            else -> throw IllegalArgumentException("Unknown PE32Magic: $name")
        }

        val PE32 = PE32Magic(0x10b)
        val PE32Plus = PE32Magic(0x20b)
    }

    override fun toString(): String = when (this) {
        PE32 -> "PE32"
        PE32Plus -> "PE32+"
        else -> "UNKNOWN($rawValue)"
    }
}
