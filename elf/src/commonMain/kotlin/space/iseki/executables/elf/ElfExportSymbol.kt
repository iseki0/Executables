package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ExportSymbol

/**
 * Represents an exported symbol in an ELF file
 *
 * @property name Symbol name
 * @property value Symbol value (usually a memory address)
 * @property size Symbol size (in bytes)
 * @property binding Symbol binding attribute (LOCAL, GLOBAL, WEAK)
 * @property type Symbol type (NOTYPE, OBJECT, FUNC, etc.)
 * @property visibility Symbol visibility (DEFAULT, HIDDEN, PROTECTED)
 */
@Serializable
data class ElfExportSymbol internal constructor(
    override val name: String,
    val value: ULong,
    val size: ULong,
    val binding: ElfSymBinding,
    val type: ElfSymType,
    val visibility: ElfSymVisibility,
) : ExportSymbol {
    override val fields: Map<String, Any>
        get() = mapOf(
            "name" to name,
            "value" to value,
            "size" to size,
            "binding" to binding,
            "type" to type,
            "visibility" to visibility,
        )
}