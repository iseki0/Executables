package space.iseki.executables.pe

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
value class DllCharacteristics(val rawValue: Short) : Set<DllCharacteristics> {
    object Constants {
        const val HIGH_ENTROPY_VA = 0x0020.toShort()
        const val DYNAMIC_BASE = 0x0040.toShort()
        const val FORCE_INTEGRITY = 0x0080.toShort()
        const val NX_COMPAT = 0x0100.toShort()
        const val NO_ISOLATION = 0x0200.toShort()
        const val NO_SEH = 0x0400.toShort()
        const val NO_BIND = 0x0800.toShort()
        const val APPCONTAINER = 0x1000.toShort()
        const val WDM_DRIVER = 0x2000.toShort()
        const val GUARD_CF = 0x4000.toShort()
        const val TERMINAL_SERVER_AWARE = 0x8000.toShort()
    }

    companion object {
        val HIGH_ENTROPY_VA = DllCharacteristics(Constants.HIGH_ENTROPY_VA)
        val DYNAMIC_BASE = DllCharacteristics(Constants.DYNAMIC_BASE)
        val FORCE_INTEGRITY = DllCharacteristics(Constants.FORCE_INTEGRITY)
        val NX_COMPAT = DllCharacteristics(Constants.NX_COMPAT)
        val NO_ISOLATION = DllCharacteristics(Constants.NO_ISOLATION)
        val NO_SEH = DllCharacteristics(Constants.NO_SEH)
        val NO_BIND = DllCharacteristics(Constants.NO_BIND)
        val APPCONTAINER = DllCharacteristics(Constants.APPCONTAINER)
        val WDM_DRIVER = DllCharacteristics(Constants.WDM_DRIVER)
        val GUARD_CF = DllCharacteristics(Constants.GUARD_CF)
        val TERMINAL_SERVER_AWARE = DllCharacteristics(Constants.TERMINAL_SERVER_AWARE)

        @JvmStatic
        fun toString(rawValue: Short): String {
            return DllCharacteristics(rawValue).toString()
        }
    }

    override val size: Int
        get() = rawValue.countOneBits()

    override fun isEmpty(): Boolean {
        return rawValue == 0.toShort()
    }

    operator fun plus(other: DllCharacteristics): DllCharacteristics {
        return DllCharacteristics(rawValue or other.rawValue)
    }

    override fun iterator(): Iterator<DllCharacteristics> {
        return object : Iterator<DllCharacteristics> {
            var remaining = rawValue
            override fun hasNext(): Boolean = remaining != 0.toShort()
            override fun next(): DllCharacteristics {
                val bit = remaining and (-remaining).toShort()
                remaining = (remaining xor bit)
                return DllCharacteristics(bit)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        if (size == 1) {
            return when (rawValue) {
                Constants.HIGH_ENTROPY_VA -> "HIGH_ENTROPY_VA"
                Constants.DYNAMIC_BASE -> "DYNAMIC_BASE"
                Constants.FORCE_INTEGRITY -> "FORCE_INTEGRITY"
                Constants.NX_COMPAT -> "NX_COMPAT"
                Constants.NO_ISOLATION -> "NO_ISOLATION"
                Constants.NO_SEH -> "NO_SEH"
                Constants.NO_BIND -> "NO_BIND"
                Constants.APPCONTAINER -> "APPCONTAINER"
                Constants.WDM_DRIVER -> "WDM_DRIVER"
                Constants.GUARD_CF -> "GUARD_CF"
                Constants.TERMINAL_SERVER_AWARE -> "TERMINAL_SERVER_AWARE"
                else -> "0x" + rawValue.toHexString()
            }
        }
        return joinToString("|")
    }

    override fun containsAll(elements: Collection<DllCharacteristics>): Boolean {
        if (elements is DllCharacteristics) {
            contains(elements)
        }
        return elements.all { rawValue and it.rawValue == it.rawValue }
    }

    override fun contains(element: DllCharacteristics): Boolean {
        return rawValue and element.rawValue == element.rawValue
    }

}

