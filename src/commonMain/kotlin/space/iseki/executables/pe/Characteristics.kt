package space.iseki.executables.pe

import kotlin.experimental.and
import kotlin.experimental.xor
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
value class Characteristics(val rawValue: Short) : Set<Characteristics> {

    object Constants {
        const val IMAGE_FILE_RELOCS_STRIPPED = 0x0001.toShort()
        const val IMAGE_FILE_EXECUTABLE_IMAGE = 0x0002.toShort()
        const val IMAGE_FILE_LINE_NUMS_STRIPPED = 0x0004.toShort()
        const val IMAGE_FILE_LOCAL_SYMS_STRIPPED = 0x0008.toShort()
        const val IMAGE_FILE_AGGRESSIVE_WS_TRIM = 0x0010.toShort()
        const val IMAGE_FILE_LARGE_ADDRESS_AWARE = 0x0020.toShort()
        const val IMAGE_FILE_BYTES_REVERSED_LO = 0x0080.toShort()
        const val IMAGE_FILE_32BIT_MACHINE = 0x0100.toShort()
        const val IMAGE_FILE_DEBUG_STRIPPED = 0x0200.toShort()
        const val IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP = 0x0400.toShort()
        const val IMAGE_FILE_NET_RUN_FROM_SWAP = 0x0800.toShort()
        const val IMAGE_FILE_SYSTEM = 0x1000.toShort()
        const val IMAGE_FILE_DLL = 0x2000.toShort()
        const val IMAGE_FILE_UP_SYSTEM_ONLY = 0x4000.toShort()
        const val IMAGE_FILE_BYTES_REVERSED_HI = 0x8000.toShort()
    }

    override val size: Int
        get() = rawValue.countOneBits()

    override fun isEmpty(): Boolean = rawValue == 0.toShort()

    override fun iterator(): Iterator<Characteristics> {
        return object : Iterator<Characteristics> {
            var remaining = rawValue
            override fun hasNext(): Boolean = remaining != 0.toShort()
            override fun next(): Characteristics {
                val bit = remaining and (-remaining).toShort()
                remaining = (remaining xor bit)
                return Characteristics(bit)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        if (size == 1) {
            return when (rawValue) {
                Constants.IMAGE_FILE_RELOCS_STRIPPED -> "IMAGE_FILE_RELOCS_STRIPPED"
                Constants.IMAGE_FILE_EXECUTABLE_IMAGE -> "IMAGE_FILE_EXECUTABLE_IMAGE"
                Constants.IMAGE_FILE_LINE_NUMS_STRIPPED -> "IMAGE_FILE_LINE_NUMS_STRIPPED"
                Constants.IMAGE_FILE_LOCAL_SYMS_STRIPPED -> "IMAGE_FILE_LOCAL_SYMS_STRIPPED"
                Constants.IMAGE_FILE_AGGRESSIVE_WS_TRIM -> "IMAGE_FILE_AGGRESSIVE_WS_TRIM"
                Constants.IMAGE_FILE_LARGE_ADDRESS_AWARE -> "IMAGE_FILE_LARGE_ADDRESS_AWARE"
                Constants.IMAGE_FILE_BYTES_REVERSED_LO -> "IMAGE_FILE_BYTES_REVERSED_LO"
                Constants.IMAGE_FILE_32BIT_MACHINE -> "IMAGE_FILE_32BIT_MACHINE"
                Constants.IMAGE_FILE_DEBUG_STRIPPED -> "IMAGE_FILE_DEBUG_STRIPPED"
                Constants.IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP -> "IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP"
                Constants.IMAGE_FILE_NET_RUN_FROM_SWAP -> "IMAGE_FILE_NET_RUN_FROM_SWAP"
                Constants.IMAGE_FILE_SYSTEM -> "IMAGE_FILE_SYSTEM"
                Constants.IMAGE_FILE_DLL -> "IMAGE_FILE_DLL"
                Constants.IMAGE_FILE_UP_SYSTEM_ONLY -> "IMAGE_FILE_UP_SYSTEM_ONLY"
                Constants.IMAGE_FILE_BYTES_REVERSED_HI -> "IMAGE_FILE_BYTES_REVERSED_HI"
                else -> "0x" + rawValue.toHexString()
            }
        }
        return joinToString("|")
    }

    override fun containsAll(elements: Collection<Characteristics>): Boolean {
        if (elements is Characteristics) {
            return contains(elements)
        }
        return elements.all { contains(it) }
    }

    override fun contains(element: Characteristics): Boolean {
        return rawValue and element.rawValue == element.rawValue
    }

    companion object {
        val IMAGE_FILE_RELOCS_STRIPPED = Characteristics(Constants.IMAGE_FILE_RELOCS_STRIPPED)
        val IMAGE_FILE_EXECUTABLE_IMAGE = Characteristics(Constants.IMAGE_FILE_EXECUTABLE_IMAGE)
        val IMAGE_FILE_LINE_NUMS_STRIPPED = Characteristics(Constants.IMAGE_FILE_LINE_NUMS_STRIPPED)
        val IMAGE_FILE_LOCAL_SYMS_STRIPPED = Characteristics(Constants.IMAGE_FILE_LOCAL_SYMS_STRIPPED)
        val IMAGE_FILE_AGGRESSIVE_WS_TRIM = Characteristics(Constants.IMAGE_FILE_AGGRESSIVE_WS_TRIM)
        val IMAGE_FILE_LARGE_ADDRESS_AWARE = Characteristics(Constants.IMAGE_FILE_LARGE_ADDRESS_AWARE)
        val IMAGE_FILE_BYTES_REVERSED_LO = Characteristics(Constants.IMAGE_FILE_BYTES_REVERSED_LO)
        val IMAGE_FILE_32BIT_MACHINE = Characteristics(Constants.IMAGE_FILE_32BIT_MACHINE)
        val IMAGE_FILE_DEBUG_STRIPPED = Characteristics(Constants.IMAGE_FILE_DEBUG_STRIPPED)
        val IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP = Characteristics(Constants.IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP)
        val IMAGE_FILE_NET_RUN_FROM_SWAP = Characteristics(Constants.IMAGE_FILE_NET_RUN_FROM_SWAP)
        val IMAGE_FILE_SYSTEM = Characteristics(Constants.IMAGE_FILE_SYSTEM)
        val IMAGE_FILE_DLL = Characteristics(Constants.IMAGE_FILE_DLL)
        val IMAGE_FILE_UP_SYSTEM_ONLY = Characteristics(Constants.IMAGE_FILE_UP_SYSTEM_ONLY)
        val IMAGE_FILE_BYTES_REVERSED_HI = Characteristics(Constants.IMAGE_FILE_BYTES_REVERSED_HI)

        @JvmStatic
        fun toString(rawValue: Short): String {
            return Characteristics(rawValue).toString()
        }
    }
}
