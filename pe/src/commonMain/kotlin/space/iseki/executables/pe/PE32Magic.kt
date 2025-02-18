package space.iseki.executables.pe

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

/**
 * Represents the pe32 magic value in a pe file.
 *
 * @property rawValue the raw short value representing the magic
 */
@JvmInline
value class PE32Magic(val rawValue: Short): Comparable<PE32Magic> {
    companion object {
        /**
         * Returns the [PE32Magic] corresponding to the given name.
         *
         * @param name the name of the magic, either "PE32" or "PE32+"
         * @return a [PE32Magic] instance
         * @throws IllegalArgumentException if the name is unknown
         */
        @JvmStatic
        fun valueOf(name: String): PE32Magic = when (name) {
            "PE32" -> PE32
            "PE32+" -> PE32Plus
            else -> throw IllegalArgumentException("Unknown PE32Magic: $name")
        }

        /**
         * The pe32 magic value.
         */
        val PE32 = PE32Magic(0x10b)

        /**
         * The pe32+ magic value.
         */
        val PE32Plus = PE32Magic(0x20b)
    }

    override fun compareTo(other: PE32Magic): Int = rawValue.compareTo(other.rawValue)

    override fun toString(): String = when (this) {
        PE32 -> "PE32"
        PE32Plus -> "PE32+"
        else -> "UNKNOWN($rawValue)"
    }
}
