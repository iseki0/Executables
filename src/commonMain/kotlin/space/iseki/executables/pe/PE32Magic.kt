package space.iseki.executables.pe

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
value class PE32Magic(private val magic: Short) {
    init {
        require(isValidMagic(magic)) { "Invalid PE32Magic: $magic" }
    }

    companion object {
        object Constants {
            const val PE32: Short = 0x10b
            const val PE32Plus: Short = 0x20b
        }

        val PE32 = PE32Magic(Constants.PE32)
        val PE32Plus = PE32Magic(Constants.PE32Plus)

        @JvmStatic
        fun isValidMagic(m: Short): Boolean {
            return m == Constants.PE32 || m == Constants.PE32Plus
        }

        @JvmStatic
        fun toString(m: Short): String {
            return PE32Magic(m).toString()
        }
    }

    override fun toString(): String {
        return when (magic) {
            Constants.PE32 -> "PE32"
            Constants.PE32Plus -> "PE32+"
            else -> error("Unknown PE32Magic: $magic")
        }
    }
}
