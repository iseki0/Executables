package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ImportSymbol

/**
 * Represents an import symbol in an ELF file
 *
 * @property name Symbol name
 * @property file Name of the source file (library) of the import symbol, usually empty or inferred from the dynamic section in ELF
 * @property binding Symbol binding attribute (usually GLOBAL or WEAK)
 * @property type Symbol type (NOTYPE, OBJECT, FUNC, etc.)
 */
@Serializable
data class ElfImportSymbol(
    override val name: String,
    override val file: String,
    val binding: ElfSymBinding,
    val type: ElfSymType,
) : ImportSymbol 