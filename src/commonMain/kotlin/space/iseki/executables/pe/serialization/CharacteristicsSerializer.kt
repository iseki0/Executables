package space.iseki.executables.pe.serialization

import space.iseki.executables.pe.BitSetSerializer
import space.iseki.executables.pe.Characteristics

internal object CharacteristicsSerializer :
    BitSetSerializer<Characteristics>(UShort.MAX_VALUE.toULong(), "Characteristics", Characteristics::plus) {
    override val unit: Characteristics
        get() = Characteristics(0)

    override fun valueOfOrNull(element: String): Characteristics? = Characteristics.valueOfOrNull(element)

    override fun valueOf(element: ULong): Characteristics = Characteristics(element.toShort())
} 