package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure

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
