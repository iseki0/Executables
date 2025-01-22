package space.iseki.executables.pe

import kotlin.jvm.JvmStatic

data class CoffHeader(
    val machine: MachineType,
    val numbersOfSections: UShort,
    val timeDateStamp: TimeDataStamp32,
    val pointerToSymbolTable: Address32,
    val numbersOfSymbols: UInt,
    val sizeOfOptionalHeader: UShort,
    val characteristics: Characteristics,
) {
    constructor(
        machine: MachineType,
        numbersOfSections: UShort,
        timeDateStamp: TimeDataStamp32,
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
            val timeDateStamp = TimeDataStamp32(bytes.getUInt(offset + 4))
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

