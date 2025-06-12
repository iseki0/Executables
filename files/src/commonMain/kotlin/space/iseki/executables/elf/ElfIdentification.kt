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
data class ElfIdentification internal constructor(
    val eiClass: ElfClass,
    val eiData: ElfData,
    val eiVersion: UByte,
    val eiOsAbi: ElfOsAbi,
    val eiAbiVersion: UByte,
) : ReadableStructure {

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        internal fun parse(bytes: ByteArray, off: Int): ElfIdentification {
            // check range and size
            if (off + 16 > bytes.size) {
                throw ElfFileException(
                    "Invalid ELF identification size",
                    "offset" to off,
                    "required_size" to 16,
                    "available_size" to (bytes.size - off),
                )
            }

            // check magic numbers (EI_MAG0 through EI_MAG3)
            if (bytes[off] != 0x7F.toByte() || bytes[off + 1] != 'E'.code.toByte() || bytes[off + 2] != 'L'.code.toByte() || bytes[off + 3] != 'F'.code.toByte()) {
                throw ElfFileException(
                    "Invalid ELF magic number",
                    "magic" to bytes.sliceArray(off until off + 4).toHexString(),
                )
            }

            // Check EI_CLASS (byte 4)
            val eiClass = ElfClass(bytes[off + 4])
            if (eiClass != ElfClass.ELFCLASS32 && eiClass != ElfClass.ELFCLASS64) {
                throw ElfFileException("Invalid ELF class", "class" to eiClass)
            }

            // Check EI_DATA (byte 5)
            val eiData = ElfData(bytes[off + 5])
            if (eiData != ElfData.ELFDATA2LSB && eiData != ElfData.ELFDATA2MSB) {
                throw ElfFileException("Invalid ELF data encoding", "encoding" to eiData)
            }

            // Check EI_VERSION (byte 6)
            val eiVersion = bytes[off + 6].toUByte()
            if (eiVersion != 1u.toUByte()) {
                throw ElfFileException("Invalid ELF version, expected 1", "version" to eiVersion)
            }

            // Check EI_OSABI (byte 7)
            val eiOsAbi = ElfOsAbi(bytes[off + 7])

            // Check EI_ABIVERSION (byte 8)
            val eiAbiVersion = bytes[off + 8].toUByte()

            // Check padding bytes (EI_PAD, bytes 9-15)
            // According to the ELF specification, these bytes should be zero
            for (i in 9 until 16) {
                if (bytes[off + i] != 0.toByte()) {
                    throw ElfFileException("Invalid ELF padding byte", "index" to i, "value" to bytes[off + i])
                }
            }

            return ElfIdentification(
                eiClass = eiClass,
                eiData = eiData,
                eiVersion = eiVersion,
                eiOsAbi = eiOsAbi,
                eiAbiVersion = eiAbiVersion,
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
