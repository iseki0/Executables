package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import space.iseki.executables.common.Address32
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.common.TimeDateStamp32
import space.iseki.executables.share.u2l
import space.iseki.executables.share.u4l
import kotlin.jvm.JvmStatic

/**
 * Represents the COFF header structure.
 *
 * @property machine The number that identifies the type of target machine. For more information, see [MachineType].
 * @property numbersOfSections The number of sections. This indicates the size of the section table, which immediately follows the headers.
 * @property timeDateStamp The low 32 bits of the number of seconds since 00:00 January 1, 1970 (a C run-time time_t value), which indicates when the file was created.
 * @property pointerToSymbolTable The file offset of the COFF symbol table, or zero if no COFF symbol table is present. This value should be zero for an image because COFF debugging information is deprecated.
 * @property numbersOfSymbols The number of entries in the symbol table. This data can be used to locate the string table, which immediately follows the symbol table. This value should be zero for an image because COFF debugging information is deprecated.
 * @property sizeOfOptionalHeader The size of the optional header, which is required for executable files but not for object files. This value should be zero for an object file. For a description of the header format, see Optional Header (Image Only).
 * @property characteristics The flags that indicate the attributes of the file. For specific flag values, see [Characteristics].
 */
@Serializable
data class CoffHeader internal constructor(
    val machine: MachineType,
    val numbersOfSections: UShort,
    val timeDateStamp: TimeDateStamp32,
    val pointerToSymbolTable: Address32,
    val numbersOfSymbols: UInt,
    val sizeOfOptionalHeader: UShort,
    val characteristics: Characteristics,
) : ReadableStructure {
    constructor(
        machine: MachineType,
        numbersOfSections: UShort,
        timeDateStamp: TimeDateStamp32,
        sizeOfOptionalHeader: UShort,
        characteristics: Characteristics,
    ) : this(
        machine = machine,
        numbersOfSections = numbersOfSections,
        timeDateStamp = timeDateStamp,
        pointerToSymbolTable = Address32(0u),
        numbersOfSymbols = 0u,
        sizeOfOptionalHeader = sizeOfOptionalHeader,
        characteristics = characteristics,
    )

    override val fields: Map<String, Any>
        get() = mapOf(
            "machine" to machine,
            "numbersOfSections" to numbersOfSections,
            "timeDateStamp" to timeDateStamp,
            "pointerToSymbolTable" to pointerToSymbolTable,
            "numbersOfSymbols" to numbersOfSymbols,
            "sizeOfOptionalHeader" to sizeOfOptionalHeader,
            "characteristics" to characteristics,
        )

    companion object {
        const val LENGTH = 20

        @JvmStatic
        fun parse(bytes: ByteArray, offset: Int): CoffHeader {
            val machine = MachineType(bytes.u2l(offset).toShort())
            val numbersOfSections = bytes.u2l(offset + 2)
            val timeDateStamp = TimeDateStamp32(bytes.u4l(offset + 4))
            val pointerToSymbolTable = Address32(bytes.u4l(offset + 8))
            val numbersOfSymbols = bytes.u4l(offset + 12)
            val sizeOfOptionalHeader = bytes.u2l(offset + 16)
            val characteristics = Characteristics(bytes.u2l(offset + 18))
            return CoffHeader(
                machine,
                numbersOfSections,
                timeDateStamp,
                pointerToSymbolTable,
                numbersOfSymbols,
                sizeOfOptionalHeader,
                characteristics,
            )
        }
    }

    internal fun validate() {
        if (numbersOfSections == 0.toUShort()) {
            throw PEFileException("No sections found", "section_count" to numbersOfSections)
        }
        if (numbersOfSections > 96.toUShort()) {
            throw PEFileException("Too many sections", "section_count" to numbersOfSections)
        }
        if (sizeOfOptionalHeader < 28.toUShort()) {
            throw PEFileException("Optional header size too small", "size" to sizeOfOptionalHeader)
        }
    }
}

