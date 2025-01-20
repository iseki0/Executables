package space.iseki.executables.pe.serialization

import space.iseki.executables.pe.BitSetSerializer
import space.iseki.executables.pe.SectionFlags

internal object SectionFlagsSerializer :
    BitSetSerializer<SectionFlags>(UInt.MAX_VALUE.toULong(), "SectionFlags", { a, b -> a + b }) {
    override val unit: SectionFlags
        get() = SectionFlags(0)

    override fun valueOfOrNull(element: String): SectionFlags? = SectionFlags.valueOfOrNull(element)

    override fun valueOf(element: ULong): SectionFlags = SectionFlags(element.toInt())
} 