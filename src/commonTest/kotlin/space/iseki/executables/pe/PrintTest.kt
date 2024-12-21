package space.iseki.executables.pe

import kotlin.test.Test

class PrintTest {
    @Test
    fun test(){
        println(
            CoffHeader(
                machine = MachineType.ALPHA,
                numbersOfSections = 1u,
                timeDateStamp = TimeDataStamp32(1),
                sizeOfOptionalHeader = 0u,
                characteristics = Characteristics.IMAGE_FILE_DLL + Characteristics.IMAGE_FILE_EXECUTABLE_IMAGE,
            )
        )
        println(
            StandardHeader(
                magic = PE32Magic.PE32,
                majorLinkerVersion = 0,
                minorLinkerVersion = 0,
                sizeOfCode = 0u,
                sizeOfInitializedData = 0u,
                sizeOfUninitializedData = 0u,
                addressOfEntryPoint = Address32(0),
                baseOfCode = Address32(0),
                baseOfData = Address32(0),
            )
        )
        println(
            WindowsSpecifiedHeader(
                magic = PE32Magic.PE32,
                imageBase = Address64(0),
                sectionAlignment = 0u,
                fileAlignment = 0u,
                majorOperatingSystemVersion = 0.toUShort(),
                minorOperatingSystemVersion = 0.toUShort(),
                majorImageVersion = 0.toUShort(),
                minorImageVersion = 0.toUShort(),
                majorSubsystemVersion = 0.toUShort(),
                minorSubsystemVersion = 0.toUShort(),
                win32VersionValue = 0u,
                sizeOfImage = 0u,
                sizeOfHeaders = 0u,
                checkSum = 0u,
                subsystem = WindowsSubsystems.WINDOWS_CUI,
                dllCharacteristics = DllCharacteristics.DYNAMIC_BASE + DllCharacteristics.NX_COMPAT,
                sizeOfStackReserve = 0u,
                sizeOfStackCommit = 0u,
                sizeOfHeapReserve = 0u,
                sizeOfHeapCommit = 0u,
                loaderFlags = 0u,
                numbersOfRvaAndSizes = 1,
                exportTable = DataDirectoryItem.ZERO,
                importTable = DataDirectoryItem(1, 2),
                resourceTable = DataDirectoryItem.ZERO,
                exceptionTable = DataDirectoryItem.ZERO,
                certificateTable = DataDirectoryItem.ZERO,
                baseRelocationTable = DataDirectoryItem.ZERO,
                debug = DataDirectoryItem.ZERO,
                architecture = DataDirectoryItem.ZERO,
                globalPtr = DataDirectoryItem.ZERO,
                tlsTable = DataDirectoryItem.ZERO,
                loadConfigTable = DataDirectoryItem.ZERO,
                boundImport = DataDirectoryItem.ZERO,
                iat = DataDirectoryItem.ZERO,
                delayImportDescriptor = DataDirectoryItem.ZERO,
                clrRuntimeHeader = DataDirectoryItem.ZERO,
            )
        )

    }
}