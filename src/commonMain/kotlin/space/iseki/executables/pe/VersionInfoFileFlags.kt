package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

fun VersionInfoFileFlags(flags: UInt): VersionInfoFileFlags = VersionInfoFileFlags(flags.toInt())

@Serializable(with = VersionInfoFileFlags.Serializer::class)
@JvmInline
value class VersionInfoFileFlags(private val flags: Int) : Set<VersionInfoFileFlags> {
    internal object Serializer :
        BitSetSerializer<VersionInfoFileFlags>(UInt.MAX_VALUE.toULong(), "FileFlags", { a, b -> a + b }) {
        override val unit: VersionInfoFileFlags = VersionInfoFileFlags(0)

        override fun valueOfOrNull(element: String): VersionInfoFileFlags? = Companion.valueOfOrNull(element)

        override fun valueOf(element: ULong): VersionInfoFileFlags = VersionInfoFileFlags(element.toInt())
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

    override fun iterator(): Iterator<VersionInfoFileFlags> {
        return object : Iterator<VersionInfoFileFlags> {
            var remaining = flags
            override fun hasNext(): Boolean = remaining != 0
            override fun next(): VersionInfoFileFlags {
                val bit = remaining and (-remaining)
                remaining = remaining and bit.inv()
                return VersionInfoFileFlags(bit)
            }
        }
    }

    override fun containsAll(elements: Collection<VersionInfoFileFlags>): Boolean {
        if (elements is VersionInfoFileFlags) {
            return contains(elements)
        }
        return elements.all { contains(it) }
    }

    override fun contains(element: VersionInfoFileFlags): Boolean {
        return flags and element.flags == element.flags
    }

    operator fun plus(other: VersionInfoFileFlags): VersionInfoFileFlags {
        return VersionInfoFileFlags(flags or other.flags)
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
        val DEBUG = VersionInfoFileFlags(Constants.DEBUG)
        val RELEASE = VersionInfoFileFlags(Constants.RELEASE)
        val PRERELEASE = VersionInfoFileFlags(Constants.PRERELEASE)
        val PATCHED = VersionInfoFileFlags(Constants.PATCHED)

        private fun valueOfOrNull(element: String): VersionInfoFileFlags? = when (element) {
            "DEBUG" -> DEBUG
            "RELEASE" -> RELEASE
            "PRERELEASE" -> PRERELEASE
            "PATCHED" -> PATCHED
            else -> null
        }
    }
} 