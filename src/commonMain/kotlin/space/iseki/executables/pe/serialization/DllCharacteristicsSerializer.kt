package space.iseki.executables.pe.serialization

import space.iseki.executables.pe.BitSetSerializer
import space.iseki.executables.pe.DllCharacteristics

internal object DllCharacteristicsSerializer :
    BitSetSerializer<DllCharacteristics>(UShort.MAX_VALUE.toULong(), "DllCharacteristics", DllCharacteristics::plus) {
    override val unit: DllCharacteristics
        get() = DllCharacteristics(0)

    override fun valueOfOrNull(element: String): DllCharacteristics? = DllCharacteristics.valueOfOrNull(element)

    override fun valueOf(element: ULong): DllCharacteristics = DllCharacteristics(element.toShort())
} 