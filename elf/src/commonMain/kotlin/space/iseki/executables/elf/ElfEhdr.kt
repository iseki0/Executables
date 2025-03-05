@file:JvmName("-ElfEhdr")

package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import kotlin.jvm.JvmName

@Serializable
sealed interface ElfEhdr : ReadableStructure {
    val eType: ElfType
    val eMachine: ElfMachine
    val eVersion: Primitive
    val eEntry: Primitive
    val ePhoff: Primitive
    val eShoff: Primitive
    val eFlags: Primitive
    val eEhsize: Primitive
    val ePhentsize: Primitive
    val ePhnum: Primitive
    val eShentsize: Primitive
    val eShnum: Primitive
    val eShstrndx: Primitive

    override val fields: Map<String, Any>
        get() = mapOf(
            "eType" to eType,
            "eMachine" to eMachine,
            "eVersion" to eVersion,
            "eEntry" to eEntry,
            "ePhoff" to ePhoff,
            "eShoff" to eShoff,
            "eFlags" to eFlags,
            "eEhsize" to eEhsize,
            "ePhentsize" to ePhentsize,
            "ePhnum" to ePhnum,
            "eShentsize" to eShentsize,
            "eShnum" to eShnum,
            "eShstrndx" to eShstrndx,
        )


}

/**
 * Validates the ELF header fields for consistency and correctness.
 *
 * @param fileSize The size of the file in bytes
 * @throws ElfFileException if the header is invalid
 */
internal fun ElfEhdr.validate(fileSize: Long) {
    // Validate e_type
    when (eType) {
        ElfType.ET_NONE -> throw ElfFileException("Invalid file type: ET_NONE")
        ElfType.ET_REL, ElfType.ET_EXEC, ElfType.ET_DYN, ElfType.ET_CORE -> { /* Valid types */
        }

        else -> {
            // Check if it's a processor-specific type (between ET_LOPROC and ET_HIPROC)
            val typeValue = eType.value.toInt()
            if (typeValue < ElfType.ET_LOPROC.value.toInt() || typeValue > ElfType.ET_HIPROC.value.toInt()) {
                throw ElfFileException("Unknown file type: $eType")
            }
        }
    }

    // Validate e_version
    if (eVersion.castToInt() != 1) {
        throw ElfFileException("Invalid ELF version: ${eVersion.castToInt()}, expected 1")
    }

    // Validate e_ehsize (ELF header size)
    val expectedEhdrSize = when (this) {
        is Elf32Ehdr -> 52
        is Elf64Ehdr -> 64
    }

    if (eEhsize.castToInt() != expectedEhdrSize) {
        throw ElfFileException("Invalid ELF header size: ${eEhsize.castToInt()}, expected $expectedEhdrSize")
    }

    // Validate offsets are within file bounds
    when (this) {
        is Elf32Ehdr -> {
            if (ePhoff.value != 0u && ePhoff.value.toLong() >= fileSize) {
                throw ElfFileException("Program header table offset ${ePhoff.value} is beyond file end ($fileSize)")
            }
            if (eShoff.value != 0u && eShoff.value.toLong() >= fileSize) {
                throw ElfFileException("Section header table offset ${eShoff.value} is beyond file end ($fileSize)")
            }
        }

        is Elf64Ehdr -> {
            if (ePhoff.value != 0UL && ePhoff.value.toLong() >= fileSize) {
                throw ElfFileException("Program header table offset ${ePhoff.value} is beyond file end ($fileSize)")
            }
            if (eShoff.value != 0UL && eShoff.value.toLong() >= fileSize) {
                throw ElfFileException("Section header table offset ${eShoff.value} is beyond file end ($fileSize)")
            }
        }
    }

    // Validate section header string table index
    val shnum = eShnum.castToInt()
    val shstrndx = eShstrndx.castToInt()

    if (shnum in 1..shstrndx) {
        throw ElfFileException("Section header string table index ($shstrndx) is out of bounds (section count: $shnum)")
    }
}