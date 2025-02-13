package space.iseki.executables.pe

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import space.iseki.executables.pe.serializer.CharacteristicsSerializer
import space.iseki.executables.pe.serializer.CoffHeaderSerializer
import space.iseki.executables.pe.serializer.StandardHeaderSerializer
import space.iseki.executables.pe.serializer.WindowsSpecifiedHeaderSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsonTest {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    @Test
    fun testCharacteristicsJson() {
        val a = Characteristics.IMAGE_FILE_EXECUTABLE_IMAGE + Characteristics.IMAGE_FILE_DLL
        val jsonText = json.encodeToString(CharacteristicsSerializer, a)
        println(jsonText)
        val b = json.decodeFromString(CharacteristicsSerializer, jsonText)
        assertEquals(a, b)
    }

    @Test
    fun testCharacteristicsFail() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(CharacteristicsSerializer, """["A"]""")
        }.printStackTrace()
    }

    @Test
    fun testCoffHeader() {
        val coffHeader = CoffHeader(
            machine = MachineType.ALPHA,
            numbersOfSections = 1u,
            timeDateStamp = TimeDataStamp32(1u),
            sizeOfOptionalHeader = 0u,
            characteristics = Characteristics.IMAGE_FILE_EXECUTABLE_IMAGE + Characteristics.IMAGE_FILE_DLL,
        )
        val jsonText = json.encodeToString(CoffHeaderSerializer, coffHeader)
        println(jsonText)
        val b = json.decodeFromString(CoffHeaderSerializer, jsonText)
        assertEquals(coffHeader, b)
    }

    @Test
    fun testStandardHeader() {
        val standardHeader = StandardHeader(
            magic = PE32Magic.PE32,
            majorLinkerVersion = 1,
            minorLinkerVersion = 0,
            sizeOfCode = 1u,
            sizeOfInitializedData = 1u,
            sizeOfUninitializedData = 1u,
            addressOfEntryPoint = Address32(1u),
            baseOfCode = Address32(1u),
            baseOfData = Address32(0u),
        )
        val jsonText = json.encodeToString(StandardHeaderSerializer, standardHeader)
        println(jsonText)
        val b = json.decodeFromString(StandardHeaderSerializer, jsonText)
        assertEquals(standardHeader, b)
    }

    @Test
    fun testWindowsSpecifiedHeader() {
        val windowsSpecifiedHeader = WindowsSpecifiedHeader(
            magic = PE32Magic.PE32,
            imageBase = Address64(0x400000),
            sectionAlignment = 0x1000u,
            fileAlignment = 0x200u,
            majorOperatingSystemVersion = 4.toUShort(),
            minorOperatingSystemVersion = 0.toUShort(),
            majorImageVersion = 0.toUShort(),
            minorImageVersion = 0.toUShort(),
            majorSubsystemVersion = 4.toUShort(),
            minorSubsystemVersion = 0.toUShort(),
            win32VersionValue = 0.toUInt(),
            sizeOfImage = 0x1000u,
            sizeOfHeaders = 0x400u,
            checkSum = 0.toUInt(),
            subsystem = WindowsSubsystems.WINDOWS_CUI,
            dllCharacteristics = DllCharacteristics.DYNAMIC_BASE,
            sizeOfStackReserve = 0x100000u,
            sizeOfStackCommit = 0x1000u,
            sizeOfHeapReserve = 0x100000u,
            sizeOfHeapCommit = 0x1000u,
            loaderFlags = 0.toUInt(),
            numbersOfRvaAndSizes = 0,
            exportTable = DataDirectoryItem(0u, 0u),
            importTable = DataDirectoryItem(0u, 0u),
            resourceTable = DataDirectoryItem(0u, 0u),
            exceptionTable = DataDirectoryItem(0u, 0u),
            certificateTable = DataDirectoryItem(0u, 0u),
            baseRelocationTable = DataDirectoryItem(0u, 0u),
            debug = DataDirectoryItem(0u, 0u),
            architecture = DataDirectoryItem(0u, 0u),
            globalPtr = DataDirectoryItem(0u, 0u),
            tlsTable = DataDirectoryItem(0u, 0u),
            loadConfigTable = DataDirectoryItem(0u, 0u),
            boundImport = DataDirectoryItem(0u, 0u),
            iat = DataDirectoryItem(0u, 0u),
            delayImportDescriptor = DataDirectoryItem(0u, 0u),
            clrRuntimeHeader = DataDirectoryItem(0u, 0u),
        )
        val jsonText = json.encodeToString(WindowsSpecifiedHeaderSerializer, windowsSpecifiedHeader)
        println(jsonText)
        val b = json.decodeFromString(WindowsSpecifiedHeaderSerializer, jsonText)
        assertEquals(windowsSpecifiedHeader, b)
    }
}