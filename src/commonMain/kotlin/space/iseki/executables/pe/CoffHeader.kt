package space.iseki.executables.pe

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
        pointerToSymbolTable = Address32(0),
        numbersOfSymbols = 0u,
        sizeOfOptionalHeader = sizeOfOptionalHeader,
        characteristics = characteristics
    )
}

