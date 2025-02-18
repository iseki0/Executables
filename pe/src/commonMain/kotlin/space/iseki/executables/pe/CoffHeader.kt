package space.iseki.executables.pe

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
data class CoffHeader(
    val machine: MachineType,
    val numbersOfSections: UShort,
    val timeDateStamp: TimeDateStamp32,
    val pointerToSymbolTable: Address32,
    val numbersOfSymbols: UInt,
    val sizeOfOptionalHeader: UShort,
    val characteristics: Characteristics,
) {
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
        characteristics = characteristics
    )

    val fields: Map<String, Any>
        get() = mapOf(
            "machine" to machine,
            "numbersOfSections" to numbersOfSections,
            "timeDateStamp" to timeDateStamp,
            "pointerToSymbolTable" to pointerToSymbolTable,
            "numbersOfSymbols" to numbersOfSymbols,
            "sizeOfOptionalHeader" to sizeOfOptionalHeader,
            "characteristics" to characteristics,
        )

    override fun toString(): String {
        return """
            |CoffHeader(
            |   machine = $machine,
            |   numbersOfSections = $numbersOfSections,
            |   timeDateStamp = $timeDateStamp,
            |   pointerToSymbolTable = $pointerToSymbolTable,
            |   numbersOfSymbols = $numbersOfSymbols,
            |   sizeOfOptionalHeader = $sizeOfOptionalHeader,
            |   characteristics = $characteristics,
            |)
        """.trimMargin()
    }

    companion object {
        const val LENGTH = 20

        @JvmStatic
        fun parse(bytes: ByteArray, offset: Int): CoffHeader {
            val machine = MachineType(bytes.getUShort(offset).toShort())
            val numbersOfSections = bytes.getUShort(offset + 2)
            val timeDateStamp = TimeDateStamp32(bytes.getUInt(offset + 4))
            val pointerToSymbolTable = Address32(bytes.getUInt(offset + 8))
            val numbersOfSymbols = bytes.getUInt(offset + 12)
            val sizeOfOptionalHeader = bytes.getUShort(offset + 16)
            val characteristics = Characteristics(bytes.getUShort(offset + 18))
            return CoffHeader(
                machine,
                numbersOfSections,
                timeDateStamp,
                pointerToSymbolTable,
                numbersOfSymbols,
                sizeOfOptionalHeader,
                characteristics
            )
        }
    }
}

