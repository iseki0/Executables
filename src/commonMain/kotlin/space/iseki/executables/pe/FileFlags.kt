package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

fun FileFlags(flags: UInt): FileFlags = FileFlags(flags.toInt())

@Serializable(with = FileFlags.Serializer::class)
@JvmInline
value class FileFlags(private val flags: Int) : Set<FileFlags> {
    internal object Serializer :
        BitSetSerializer<FileFlags>(UInt.MAX_VALUE.toULong(), "FileFlags", { a, b -> a + b }) {
        override val unit: FileFlags = FileFlags(0)

        override fun valueOfOrNull(element: String): FileFlags? = Companion.valueOfOrNull(element)

        override fun valueOf(element: ULong): FileFlags = FileFlags(element.toInt())
    }

    object Constants {
        const val DEBUG = 0x1u
        const val RELEASE = 0x2u
        const val PRERELEASE = 0x4u
        const val PATCHED = 0x8u
    }

    override val size: Int
        get() = flags.countOneBits()

    override fun isEmpty(): Boolean = flags == 0

    override fun iterator(): Iterator<FileFlags> {
        return object : Iterator<FileFlags> {
            var remaining = flags
            override fun hasNext(): Boolean = remaining != 0
            override fun next(): FileFlags {
                val bit = remaining and (-remaining)
                remaining = remaining and bit.inv()
                return FileFlags(bit)
            }
        }
    }

    override fun containsAll(elements: Collection<FileFlags>): Boolean {
        if (elements is FileFlags) {
            return contains(elements)
        }
        return elements.all { contains(it) }
    }

    override fun contains(element: FileFlags): Boolean {
        return flags and element.flags == element.flags
    }

    operator fun plus(other: FileFlags): FileFlags {
        return FileFlags(flags or other.flags)
    }

    override fun toString(): String {
        if (size == 1) {
            return when (flags.toUInt()) {
                Constants.DEBUG -> "DEBUG"
                Constants.RELEASE -> "RELEASE"
                Constants.PRERELEASE -> "PRERELEASE"
                Constants.PATCHED -> "PATCHED"
                else -> "0x${flags.toString(16)}"
            }
        }
        return joinToString("|")
    }

    companion object {
        val DEBUG = FileFlags(Constants.DEBUG)
        val RELEASE = FileFlags(Constants.RELEASE)
        val PRERELEASE = FileFlags(Constants.PRERELEASE)
        val PATCHED = FileFlags(Constants.PATCHED)

        private fun valueOfOrNull(element: String): FileFlags? = when (element) {
            "DEBUG" -> DEBUG
            "RELEASE" -> RELEASE
            "PRERELEASE" -> PRERELEASE
            "PATCHED" -> PATCHED
            else -> null
        }
    }
} 