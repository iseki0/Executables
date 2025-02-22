package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

/**
 * Represents the initial bytes of an ELF file that specify how to interpret the file.
 *
 * The ELF identification provides an object file framework to support multiple processors,
 * multiple data encodings, and multiple classes of machines. These initial bytes help interpret
 * the file independent of the processor and the file's remaining contents.
 *
 * @property eiClass Identifies the file's class or capacity (32-bit or 64-bit objects).
 * @property eiData Specifies the data encoding of processor-specific data (little or big endian).
 * @property eiVersion Specifies the ELF header version number.
 * @property eiOsAbi Identifies the operating system and ABI for which the object is prepared.
 * @property eiAbiVersion The ABI version.
 */
@Serializable
data class ElfIdentification(
    val eiClass: ElfClass,
    val eiData: ElfData,
    val eiVersion: UByte,
    val eiOsAbi: ElfOsAbi,
    val eiAbiVersion: UByte,
) : ReadableStructure {

    companion object {
        internal fun parse(bytes: ByteArray, off: Int): ElfIdentification {
            // check range and size
            if (off + 16 > bytes.size) {
                throw ElfFileException("Invalid ELF identification size")
            }
            // check magic
            if (bytes[off] != 0x7F.toByte() || bytes[off + 1] != 'E'.code.toByte() || bytes[off + 2] != 'L'.code.toByte() || bytes[off + 3] != 'F'.code.toByte()) {
                throw ElfFileException("Invalid ELF magic")
            }
            return ElfIdentification(
                eiClass = ElfClass(bytes[off + 4]),
                eiData = ElfData(bytes[off + 5]),
                eiVersion = bytes[off + 6].toUByte(),
                eiOsAbi = ElfOsAbi(bytes[off + 7]),
                eiAbiVersion = bytes[off + 8].toUByte(),
            )
        }
    }

    override val fields: Map<String, Any>
        get() = mapOf(
            "eiClass" to eiClass,
            "eiData" to eiData,
            "eiVersion" to eiVersion,
            "eiOsAbi" to eiOsAbi,
            "eiAbiVersion" to eiAbiVersion,
        )

}
