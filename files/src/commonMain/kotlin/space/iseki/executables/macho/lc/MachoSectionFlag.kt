package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoSectionAttributes
import space.iseki.executables.macho.MachoSectionType
import kotlin.jvm.JvmInline

@JvmInline
value class MachoSectionFlag(val rawValue: UInt) {
    val type: MachoSectionType
        get() = MachoSectionType(rawValue and 0xFFu)
    val attributes: MachoSectionAttributes
        get() = MachoSectionAttributes(rawValue and 0xFFFFFF00u)

    override fun toString(): String {
        return "(type=$type, attributes=$attributes)"
    }
}
