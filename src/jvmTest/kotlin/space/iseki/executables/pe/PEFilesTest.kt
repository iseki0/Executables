package space.iseki.executables.pe

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.intellij.lang.annotations.Language
import space.iseki.executables.pe.serialization.PEFileSummarySerializer
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.isRegularFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@Language("json")
private val jsonRef = """
    {
        "coffHeader": {
            "machine": "AMD64",
            "numbersOfSections": 6,
            "timeDateStamp": "1991-04-29T15:12:57Z",
            "pointerToSymbolTable": "0x00000000",
            "numbersOfSymbols": 0,
            "sizeOfOptionalHeader": 240,
            "characteristics": [
                "IMAGE_FILE_EXECUTABLE_IMAGE",
                "IMAGE_FILE_LARGE_ADDRESS_AWARE"
            ]
        },
        "standardHeader": {
            "magic": "PE32+",
            "majorLinkerVersion": 14,
            "minorLinkerVersion": 37,
            "sizeOfCode": 3584,
            "sizeOfInitializedData": 34816,
            "sizeOfUninitializedData": 0,
            "addressOfEntryPoint": "0x00001434",
            "baseOfCode": "0x00001000",
            "baseOfData": "0x00000000"
        },
        "windowsHeader": {
            "magic": "PE32+",
            "imageBase": "0x0000000040000000",
            "sectionAlignment": 4096,
            "fileAlignment": 512,
            "majorOperatingSystemVersion": 6,
            "minorOperatingSystemVersion": 0,
            "majorImageVersion": 0,
            "minorImageVersion": 0,
            "majorSubsystemVersion": 6,
            "minorSubsystemVersion": 0,
            "win32VersionValue": 0,
            "sizeOfImage": 57344,
            "sizeOfHeaders": 1024,
            "checkSum": 85624,
            "subsystem": "WINDOWS_CUI",
            "dllCharacteristics": [
                "HIGH_ENTROPY_VA",
                "DYNAMIC_BASE",
                "NX_COMPAT",
                "TERMINAL_SERVER_AWARE"
            ],
            "sizeOfStackReserve": 1048576,
            "sizeOfStackCommit": 4096,
            "sizeOfHeapReserve": 1048576,
            "sizeOfHeapCommit": 4096,
            "loaderFlags": 0,
            "numbersOfRvaAndSizes": 16,
            "exportTable": {
                "virtualAddress": "0x00002940",
                "size": 64
            },
            "importTable": {
                "virtualAddress": "0x00002980",
                "size": 200
            },
            "resourceTable": {
                "virtualAddress": "0x00006000",
                "size": 28232
            },
            "exceptionTable": {
                "virtualAddress": "0x00005000",
                "size": 324
            },
            "certificateTable": {
                "virtualAddress": "0x00009a00",
                "size": 12512
            },
            "baseRelocationTable": {
                "virtualAddress": "0x0000d000",
                "size": 56
            },
            "debug": {
                "virtualAddress": "0x00002440",
                "size": 112
            },
            "architecture": {
                "virtualAddress": "0x00000000",
                "size": 0
            },
            "globalPtr": {
                "virtualAddress": "0x00000000",
                "size": 0
            },
            "tlsTable": {
                "virtualAddress": "0x00000000",
                "size": 0
            },
            "loadConfigTable": {
                "virtualAddress": "0x00002300",
                "size": 320
            },
            "boundImport": {
                "virtualAddress": "0x00000000",
                "size": 0
            },
            "iat": {
                "virtualAddress": "0x00002000",
                "size": 480
            },
            "delayImportDescriptor": {
                "virtualAddress": "0x00000000",
                "size": 0
            },
            "clrRuntimeHeader": {
                "virtualAddress": "0x00000000",
                "size": 0
            }
        },
        "sectionTable": [
            {
                "name": ".text",
                "virtualSize": 3356,
                "virtualAddress": "0x00001000",
                "sizeOfRawData": 3584,
                "pointerToRawData": "0x00000400",
                "pointerToRelocations": "0x00000000",
                "pointerToLinenumbers": "0x00000000",
                "numberOfRelocations": 0,
                "numberOfLinenumbers": 0,
                "characteristics": [
                    "IMAGE_SCN_CNT_CODE",
                    "IMAGE_SCN_MEM_EXECUTE",
                    "IMAGE_SCN_MEM_READ"
                ]
            },
            {
                "name": ".rdata",
                "virtualSize": 4372,
                "virtualAddress": "0x00002000",
                "sizeOfRawData": 4608,
                "pointerToRawData": "0x00001200",
                "pointerToRelocations": "0x00000000",
                "pointerToLinenumbers": "0x00000000",
                "numberOfRelocations": 0,
                "numberOfLinenumbers": 0,
                "characteristics": [
                    "IMAGE_SCN_CNT_INITIALIZED_DATA",
                    "IMAGE_SCN_MEM_READ"
                ]
            },
            {
                "name": ".data",
                "virtualSize": 216,
                "virtualAddress": "0x00004000",
                "sizeOfRawData": 512,
                "pointerToRawData": "0x00002400",
                "pointerToRelocations": "0x00000000",
                "pointerToLinenumbers": "0x00000000",
                "numberOfRelocations": 0,
                "numberOfLinenumbers": 0,
                "characteristics": [
                    "IMAGE_SCN_CNT_INITIALIZED_DATA",
                    "IMAGE_SCN_MEM_READ",
                    "IMAGE_SCN_MEM_WRITE"
                ]
            },
            {
                "name": ".pdata",
                "virtualSize": 324,
                "virtualAddress": "0x00005000",
                "sizeOfRawData": 512,
                "pointerToRawData": "0x00002600",
                "pointerToRelocations": "0x00000000",
                "pointerToLinenumbers": "0x00000000",
                "numberOfRelocations": 0,
                "numberOfLinenumbers": 0,
                "characteristics": [
                    "IMAGE_SCN_CNT_INITIALIZED_DATA",
                    "IMAGE_SCN_MEM_READ"
                ]
            },
            {
                "name": ".rsrc",
                "virtualSize": 28232,
                "virtualAddress": "0x00006000",
                "sizeOfRawData": 28672,
                "pointerToRawData": "0x00002800",
                "pointerToRelocations": "0x00000000",
                "pointerToLinenumbers": "0x00000000",
                "numberOfRelocations": 0,
                "numberOfLinenumbers": 0,
                "characteristics": [
                    "IMAGE_SCN_CNT_INITIALIZED_DATA",
                    "IMAGE_SCN_MEM_READ"
                ]
            },
            {
                "name": ".reloc",
                "virtualSize": 56,
                "virtualAddress": "0x0000d000",
                "sizeOfRawData": 512,
                "pointerToRawData": "0x00009800",
                "pointerToRelocations": "0x00000000",
                "pointerToLinenumbers": "0x00000000",
                "numberOfRelocations": 0,
                "numberOfLinenumbers": 0,
                "characteristics": [
                    "IMAGE_SCN_CNT_INITIALIZED_DATA",
                    "IMAGE_SCN_MEM_DISCARDABLE",
                    "IMAGE_SCN_MEM_READ"
                ]
            }
        ]
    }
""".trimIndent()

class PEFilesTest {
    @Test
    fun test() {
        println(Path.of(".").absolute())
        val java = Path.of("src/jvmTest/resources/java.exe")
        assertTrue { java.isRegularFile() }
        val json = Json { prettyPrint = true }
        PEFile(java).use { pe ->
            val jsonText = json.encodeToString(PEFileSummarySerializer, pe.summary)
            println(jsonText)
            val summary = json.decodeFromString(PEFileSummarySerializer, jsonText)
            assertEquals(pe.summary, summary)
            assertEquals(json.decodeFromString<JsonElement>(jsonRef), json.decodeFromString<JsonElement>(jsonText))
        }
    }

}
