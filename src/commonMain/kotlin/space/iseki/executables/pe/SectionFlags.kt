package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable(with = SectionFlags.Serializer::class)
@JvmInline
value class SectionFlags(private val rawValue: Int) : Set<SectionFlags> {
    internal object Serializer :
        BitSetSerializer<SectionFlags>(UInt.MAX_VALUE.toULong(), "SectionFlags", { a, b -> a + b }) {
        override val unit: SectionFlags
            get() = SectionFlags(0)

        override fun valueOfOrNull(element: String): SectionFlags? = when (element) {
            "IMAGE_SCN_TYPE_NO_PAD" -> IMAGE_SCN_TYPE_NO_PAD
            "IMAGE_SCN_CNT_CODE" -> IMAGE_SCN_CNT_CODE
            "IMAGE_SCN_CNT_INITIALIZED_DATA" -> IMAGE_SCN_CNT_INITIALIZED_DATA
            "IMAGE_SCN_CNT_UNINITIALIZED_DATA" -> IMAGE_SCN_CNT_UNINITIALIZED_DATA
            "IMAGE_SCN_LNK_OTHER" -> IMAGE_SCN_LNK_OTHER
            "IMAGE_SCN_LNK_INFO" -> IMAGE_SCN_LNK_INFO
            "IMAGE_SCN_LNK_REMOVE" -> IMAGE_SCN_LNK_REMOVE
            "IMAGE_SCN_LNK_COMDAT" -> IMAGE_SCN_LNK_COMDAT
            "IMAGE_SCN_GPREL" -> IMAGE_SCN_GPREL
            "IMAGE_SCN_MEM_PURGEABLE" -> IMAGE_SCN_MEM_PURGEABLE
            "IMAGE_SCN_MEM_16BIT" -> IMAGE_SCN_MEM_16BIT
            "IMAGE_SCN_MEM_LOCKED" -> IMAGE_SCN_MEM_LOCKED
            "IMAGE_SCN_MEM_PRELOAD" -> IMAGE_SCN_MEM_PRELOAD
            "IMAGE_SCN_ALIGN_1BYTES" -> IMAGE_SCN_ALIGN_1BYTES
            "IMAGE_SCN_ALIGN_2BYTES" -> IMAGE_SCN_ALIGN_2BYTES
            "IMAGE_SCN_ALIGN_4BYTES" -> IMAGE_SCN_ALIGN_4BYTES
            "IMAGE_SCN_ALIGN_8BYTES" -> IMAGE_SCN_ALIGN_8BYTES
            "IMAGE_SCN_ALIGN_16BYTES" -> IMAGE_SCN_ALIGN_16BYTES
            "IMAGE_SCN_ALIGN_32BYTES" -> IMAGE_SCN_ALIGN_32BYTES
            "IMAGE_SCN_ALIGN_64BYTES" -> IMAGE_SCN_ALIGN_64BYTES
            "IMAGE_SCN_ALIGN_128BYTES" -> IMAGE_SCN_ALIGN_128BYTES
            "IMAGE_SCN_ALIGN_256BYTES" -> IMAGE_SCN_ALIGN_256BYTES
            "IMAGE_SCN_ALIGN_512BYTES" -> IMAGE_SCN_ALIGN_512BYTES
            "IMAGE_SCN_ALIGN_1024BYTES" -> IMAGE_SCN_ALIGN_1024BYTES
            "IMAGE_SCN_ALIGN_2048BYTES" -> IMAGE_SCN_ALIGN_2048BYTES
            "IMAGE_SCN_ALIGN_4096BYTES" -> IMAGE_SCN_ALIGN_4096BYTES
            "IMAGE_SCN_ALIGN_8192BYTES" -> IMAGE_SCN_ALIGN_8192BYTES
            "IMAGE_SCN_LNK_NRELOC_OVFL" -> IMAGE_SCN_LNK_NRELOC_OVFL
            "IMAGE_SCN_MEM_DISCARDABLE" -> IMAGE_SCN_MEM_DISCARDABLE
            "IMAGE_SCN_MEM_NOT_CACHED" -> IMAGE_SCN_MEM_NOT_CACHED
            "IMAGE_SCN_MEM_NOT_PAGED" -> IMAGE_SCN_MEM_NOT_PAGED
            "IMAGE_SCN_MEM_SHARED" -> IMAGE_SCN_MEM_SHARED
            "IMAGE_SCN_MEM_EXECUTE" -> IMAGE_SCN_MEM_EXECUTE
            "IMAGE_SCN_MEM_READ" -> IMAGE_SCN_MEM_READ
            "IMAGE_SCN_MEM_WRITE" -> IMAGE_SCN_MEM_WRITE
            else -> null
        }

        override fun valueOf(element: ULong): SectionFlags = SectionFlags(element.toInt())

    }

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
        const val IMAGE_SCN_ALIGN_32BYTES = 0x00600000
        const val IMAGE_SCN_ALIGN_64BYTES = 0x00700000
        const val IMAGE_SCN_ALIGN_128BYTES = 0x00800000
        const val IMAGE_SCN_ALIGN_256BYTES = 0x00900000
        const val IMAGE_SCN_ALIGN_512BYTES = 0x00A00000
        const val IMAGE_SCN_ALIGN_1024BYTES = 0x00B00000
        const val IMAGE_SCN_ALIGN_2048BYTES = 0x00C00000
        const val IMAGE_SCN_ALIGN_4096BYTES = 0x00D00000
        const val IMAGE_SCN_ALIGN_8192BYTES = 0x00E00000
        const val IMAGE_SCN_LNK_NRELOC_OVFL = 0x01000000
        const val IMAGE_SCN_MEM_DISCARDABLE = 0x02000000
        const val IMAGE_SCN_MEM_NOT_CACHED = 0x04000000
        const val IMAGE_SCN_MEM_NOT_PAGED = 0x08000000
        const val IMAGE_SCN_MEM_SHARED = 0x10000000
        const val IMAGE_SCN_MEM_EXECUTE = 0x20000000
        const val IMAGE_SCN_MEM_READ = 0x40000000
        const val IMAGE_SCN_MEM_WRITE = 0x80000000.toInt()

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
        val IMAGE_SCN_ALIGN_32BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_32BYTES)
        val IMAGE_SCN_ALIGN_64BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_64BYTES)
        val IMAGE_SCN_ALIGN_128BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_128BYTES)
        val IMAGE_SCN_ALIGN_256BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_256BYTES)
        val IMAGE_SCN_ALIGN_512BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_512BYTES)
        val IMAGE_SCN_ALIGN_1024BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_1024BYTES)
        val IMAGE_SCN_ALIGN_2048BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_2048BYTES)
        val IMAGE_SCN_ALIGN_4096BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_4096BYTES)
        val IMAGE_SCN_ALIGN_8192BYTES = SectionFlags(Constants.IMAGE_SCN_ALIGN_8192BYTES)
        val IMAGE_SCN_LNK_NRELOC_OVFL = SectionFlags(Constants.IMAGE_SCN_LNK_NRELOC_OVFL)
        val IMAGE_SCN_MEM_DISCARDABLE = SectionFlags(Constants.IMAGE_SCN_MEM_DISCARDABLE)
        val IMAGE_SCN_MEM_NOT_CACHED = SectionFlags(Constants.IMAGE_SCN_MEM_NOT_CACHED)
        val IMAGE_SCN_MEM_NOT_PAGED = SectionFlags(Constants.IMAGE_SCN_MEM_NOT_PAGED)
        val IMAGE_SCN_MEM_SHARED = SectionFlags(Constants.IMAGE_SCN_MEM_SHARED)
        val IMAGE_SCN_MEM_EXECUTE = SectionFlags(Constants.IMAGE_SCN_MEM_EXECUTE)
        val IMAGE_SCN_MEM_READ = SectionFlags(Constants.IMAGE_SCN_MEM_READ)
        val IMAGE_SCN_MEM_WRITE = SectionFlags(Constants.IMAGE_SCN_MEM_WRITE)
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
        return rawValue and element.rawValue != 0
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
                Constants.IMAGE_SCN_ALIGN_32BYTES -> "IMAGE_SCN_ALIGN_32BYTES"
                Constants.IMAGE_SCN_ALIGN_64BYTES -> "IMAGE_SCN_ALIGN_64BYTES"
                Constants.IMAGE_SCN_ALIGN_128BYTES -> "IMAGE_SCN_ALIGN_128BYTES"
                Constants.IMAGE_SCN_ALIGN_256BYTES -> "IMAGE_SCN_ALIGN_256BYTES"
                Constants.IMAGE_SCN_ALIGN_512BYTES -> "IMAGE_SCN_ALIGN_512BYTES"
                Constants.IMAGE_SCN_ALIGN_1024BYTES -> "IMAGE_SCN_ALIGN_1024BYTES"
                Constants.IMAGE_SCN_ALIGN_2048BYTES -> "IMAGE_SCN_ALIGN_2048BYTES"
                Constants.IMAGE_SCN_ALIGN_4096BYTES -> "IMAGE_SCN_ALIGN_4096BYTES"
                Constants.IMAGE_SCN_ALIGN_8192BYTES -> "IMAGE_SCN_ALIGN_8192BYTES"
                Constants.IMAGE_SCN_LNK_NRELOC_OVFL -> "IMAGE_SCN_LNK_NRELOC_OVFL"
                Constants.IMAGE_SCN_MEM_DISCARDABLE -> "IMAGE_SCN_MEM_DISCARDABLE"
                Constants.IMAGE_SCN_MEM_NOT_CACHED -> "IMAGE_SCN_MEM_NOT_CACHED"
                Constants.IMAGE_SCN_MEM_NOT_PAGED -> "IMAGE_SCN_MEM_NOT_PAGED"
                Constants.IMAGE_SCN_MEM_SHARED -> "IMAGE_SCN_MEM_SHARED"
                Constants.IMAGE_SCN_MEM_EXECUTE -> "IMAGE_SCN_MEM_EXECUTE"
                Constants.IMAGE_SCN_MEM_READ -> "IMAGE_SCN_MEM_READ"
                Constants.IMAGE_SCN_MEM_WRITE -> "IMAGE_SCN_MEM_WRITE"
                else -> "0x" + rawValue.toHexString()
            }
        }
        return joinToString("|")
    }
}