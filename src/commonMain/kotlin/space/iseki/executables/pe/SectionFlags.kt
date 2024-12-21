package space.iseki.executables.pe

import kotlin.jvm.JvmInline

@JvmInline
value class SectionFlags(private val rawValue: Int) : Set<SectionFlags> {
    object Constants {
        const val IMAGE_SCN_TYPE_NO_PAD = 0x00000008
        const val IMAGE_SCN_CNT_CODE = 0x00000020
        const val IMAGE_SCN_CNT_INITIALIZED_DATA = 0x00000040
        const val IMAGE_SCN_CNT_UNINITIALIZED_DATA = 0x00000080
        const val IMAGE_SCN_LNK_OTHER = 0x00000100
        const val IMAGE_SCN_LNK_INFO = 0x00000200
        const val IMAGE_SCN_LNK_REMOVE = 0x00000800
        const val IMAGE_SCN_LNK_COMDAT = 0x00001000
        const val IMAGE_SCN_GPREL = 0x00008000
        const val IMAGE_SCN_MEM_PURGEABLE = 0x00020000
        const val IMAGE_SCN_MEM_16BIT = 0x00020000
        const val IMAGE_SCN_MEM_LOCKED = 0x00040000
        const val IMAGE_SCN_MEM_PRELOAD = 0x00080000
        const val IMAGE_SCN_ALIGN_1BYTES = 0x00100000
        const val IMAGE_SCN_ALIGN_2BYTES = 0x00200000
        const val IMAGE_SCN_ALIGN_4BYTES = 0x00300000
        const val IMAGE_SCN_ALIGN_8BYTES = 0x00400000
        const val IMAGE_SCN_ALIGN_16BYTES = 0x00500000
    }

    companion object {
        val IMAGE_SCN_TYPE_NO_PAD = SectionFlags(Constants.IMAGE_SCN_TYPE_NO_PAD)
        val IMAGE_SCN_CNT_CODE = SectionFlags(Constants.IMAGE_SCN_CNT_CODE)
        val IMAGE_SCN_CNT_INITIALIZED_DATA = SectionFlags(Constants.IMAGE_SCN_CNT_INITIALIZED_DATA)
        val IMAGE_SCN_CNT_UNINITIALIZED_DATA = SectionFlags(Constants.IMAGE_SCN_CNT_UNINITIALIZED_DATA)
        val IMAGE_SCN_LNK_OTHER = SectionFlags(Constants.IMAGE_SCN_LNK_OTHER)
        val IMAGE_SCN_LNK_INFO = SectionFlags(Constants.IMAGE_SCN_LNK_INFO)
        val IMAGE_SCN_LNK_REMOVE = SectionFlags(Constants.IMAGE_SCN_LNK_REMOVE)
        val IMAGE_SCN_LNK_COMDAT = SectionFlags(Constants.IMAGE_SCN_LNK_COMDAT)
        val IMAGE_SCN_GPREL = SectionFlags(Constants.IMAGE_SCN_GPREL)
        val IMAGE_SCN_MEM_PURGEABLE = SectionFlags(Constants.IMAGE_SCN_MEM_PURGEABLE)
        val IMAGE_SCN_MEM_16BIT = SectionFlags(Constants.IMAGE_SCN_MEM_16BIT)
        val IMAGE_SCN_MEM_LOCKED = SectionFlags(Constants.IMAGE_SCN_MEM_LOCKED)
        val IMAGE_SCN_MEM_PRELOAD = SectionFlags(Constants.IMAGE_SCN_MEM_PRELOAD)
        val IMAGE_SCN_ALIGN_1BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_1BYTES)
        val IMAGE_SCN_ALIGN_2BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_2BYTES)
        val IMAGE_SCN_ALIGN_4BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_4BYTES)
        val IMAGE_SCN_ALIGN_8BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_8BYTES)
        val IMAGE_SCN_ALIGN_16BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_16BYTES)
    }

    override val size: Int
        get() = rawValue.countOneBits()

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): Iterator<SectionFlags> {
        return object : Iterator<SectionFlags> {
            var remaining = rawValue
            override fun hasNext(): Boolean {
                return remaining != 0
            }

            override fun next(): SectionFlags {
                val flag = remaining and -remaining
                remaining = remaining and flag.inv()
                return SectionFlags(flag)
            }
        }
    }

    override fun containsAll(elements: Collection<SectionFlags>): Boolean {
        return if (elements is SectionFlags) contains(elements) else elements.any { contains(it) }
    }

    override fun contains(element: SectionFlags): Boolean {
        return rawValue and element.rawValue!= 0
    }

    operator fun plus(other: SectionFlags): SectionFlags {
        return SectionFlags(rawValue or other.rawValue)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        if (size == 1) {
            return when (rawValue) {
                Constants.IMAGE_SCN_TYPE_NO_PAD -> "IMAGE_SCN_TYPE_NO_PAD"
                Constants.IMAGE_SCN_CNT_CODE -> "IMAGE_SCN_CNT_CODE"
                Constants.IMAGE_SCN_CNT_INITIALIZED_DATA -> "IMAGE_SCN_CNT_INITIALIZED_DATA"
                Constants.IMAGE_SCN_CNT_UNINITIALIZED_DATA -> "IMAGE_SCN_CNT_UNINITIALIZED_DATA"
                Constants.IMAGE_SCN_LNK_OTHER -> "IMAGE_SCN_LNK_OTHER"
                Constants.IMAGE_SCN_LNK_INFO -> "IMAGE_SCN_LNK_INFO"
                Constants.IMAGE_SCN_LNK_REMOVE -> "IMAGE_SCN_LNK_REMOVE"
                Constants.IMAGE_SCN_LNK_COMDAT -> "IMAGE_SCN_LNK_COMDAT"
                Constants.IMAGE_SCN_GPREL -> "IMAGE_SCN_GPREL"
                Constants.IMAGE_SCN_MEM_PURGEABLE -> "IMAGE_SCN_MEM_PURGEABLE|IMAGE_SCN_MEM_16BIT"
                Constants.IMAGE_SCN_MEM_LOCKED -> "IMAGE_SCN_MEM_LOCKED"
                Constants.IMAGE_SCN_MEM_PRELOAD -> "IMAGE_SCN_MEM_PRELOAD"
                Constants.IMAGE_SCN_ALIGN_1BYTES -> "IMAGE_SCN_ALIGN_1BYTES"
                Constants.IMAGE_SCN_ALIGN_2BYTES -> "IMAGE_SCN_ALIGN_2BYTES"
                Constants.IMAGE_SCN_ALIGN_4BYTES -> "IMAGE_SCN_ALIGN_4BYTES"
                Constants.IMAGE_SCN_ALIGN_8BYTES -> "IMAGE_SCN_ALIGN_8BYTES"
                Constants.IMAGE_SCN_ALIGN_16BYTES -> "IMAGE_SCN_ALIGN_16BYTES"
                else -> "0x" + rawValue.toHexString()
            }
        }
        return joinToString("|")
    }
}