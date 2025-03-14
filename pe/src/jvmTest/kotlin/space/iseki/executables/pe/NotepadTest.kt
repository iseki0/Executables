package space.iseki.executables.pe

import space.iseki.executables.pe.vi.parseVersionData
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.test.Test
import kotlin.test.assertNotNull

class NotepadTest {
    @Test
    fun testOpenNotepad() {
        val notepad = Path.of(System.getenv("windir") ?: return).resolve("system32/notepad.exe")
        if (!notepad.isRegularFile()) {
            return
        }
        println(notepad)
        PEFile.open(notepad).use { peFile ->
            println(peFile.summary)
            val versionInfo = peFile.versionInfo
            assertNotNull(versionInfo)
            println(versionInfo)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    val data = """
        70 03 34 00 00 00 56 00 53 00 5F 00 56 00 45 00 
        52 00 53 00 49 00 4F 00 4E 00 5F 00 49 00 4E 00 
        46 00 4F 00 00 00 00 00 BD 04 EF FE 00 00 01 00 
        00 00 0A 00 03 11 61 4A 00 00 0A 00 03 11 61 4A 
        3F 00 00 00 00 00 00 00 04 00 04 00 01 00 00 00 
        00 00 00 00 00 00 00 00 00 00 00 00 D0 02 00 00 
        01 00 53 00 74 00 72 00 69 00 6E 00 67 00 46 00 
        69 00 6C 00 65 00 49 00 6E 00 66 00 6F 00 00 00 
        AC 02 00 00 01 00 30 00 34 00 30 00 39 00 30 00 
        34 00 42 00 30 00 00 00 4C 00 16 00 01 00 43 00 
        6F 00 6D 00 70 00 61 00 6E 00 79 00 4E 00 61 00 
        6D 00 65 00 00 00 00 00 4D 00 69 00 63 00 72 00 
        6F 00 73 00 6F 00 66 00 74 00 20 00 43 00 6F 00 
        72 00 70 00 6F 00 72 00 61 00 74 00 69 00 6F 00 
        6E 00 00 00 38 00 08 00 01 00 46 00 69 00 6C 00 
        65 00 44 00 65 00 73 00 63 00 72 00 69 00 70 00 
        74 00 69 00 6F 00 6E 00 00 00 00 00 4E 00 6F 00 
        74 00 65 00 70 00 61 00 64 00 00 00 6E 00 27 00 
        01 00 46 00 69 00 6C 00 65 00 56 00 65 00 72 00 
        73 00 69 00 6F 00 6E 00 00 00 00 00 31 00 30 00 
        2E 00 30 00 2E 00 31 00 39 00 30 00 34 00 31 00 
        2E 00 34 00 33 00 35 00 35 00 20 00 28 00 57 00 
        69 00 6E 00 42 00 75 00 69 00 6C 00 64 00 2E 00 
        31 00 36 00 30 00 31 00 30 00 31 00 2E 00 30 00 
        38 00 30 00 30 00 29 00 00 00 00 00 30 00 08 00 
        01 00 49 00 6E 00 74 00 65 00 72 00 6E 00 61 00 
        6C 00 4E 00 61 00 6D 00 65 00 00 00 4E 00 6F 00 
        74 00 65 00 70 00 61 00 64 00 00 00 80 00 2E 00 
        01 00 4C 00 65 00 67 00 61 00 6C 00 43 00 6F 00 
        70 00 79 00 72 00 69 00 67 00 68 00 74 00 00 00 
        A9 00 20 00 4D 00 69 00 63 00 72 00 6F 00 73 00 
        6F 00 66 00 74 00 20 00 43 00 6F 00 72 00 70 00 
        6F 00 72 00 61 00 74 00 69 00 6F 00 6E 00 2E 00 
        20 00 41 00 6C 00 6C 00 20 00 72 00 69 00 67 00 
        68 00 74 00 73 00 20 00 72 00 65 00 73 00 65 00 
        72 00 76 00 65 00 64 00 2E 00 00 00 40 00 0C 00 
        01 00 4F 00 72 00 69 00 67 00 69 00 6E 00 61 00 
        6C 00 46 00 69 00 6C 00 65 00 6E 00 61 00 6D 00 
        65 00 00 00 4E 00 4F 00 54 00 45 00 50 00 41 00 
        44 00 2E 00 45 00 58 00 45 00 00 00 6A 00 25 00 
        01 00 50 00 72 00 6F 00 64 00 75 00 63 00 74 00 
        4E 00 61 00 6D 00 65 00 00 00 00 00 4D 00 69 00 
        63 00 72 00 6F 00 73 00 6F 00 66 00 74 00 AE 00 
        20 00 57 00 69 00 6E 00 64 00 6F 00 77 00 73 00 
        AE 00 20 00 4F 00 70 00 65 00 72 00 61 00 74 00 
        69 00 6E 00 67 00 20 00 53 00 79 00 73 00 74 00 
        65 00 6D 00 00 00 00 00 44 00 10 00 01 00 50 00 
        72 00 6F 00 64 00 75 00 63 00 74 00 56 00 65 00 
        72 00 73 00 69 00 6F 00 6E 00 00 00 31 00 30 00 
        2E 00 30 00 2E 00 31 00 39 00 30 00 34 00 31 00 
        2E 00 34 00 33 00 35 00 35 00 00 00 44 00 00 00 
        01 00 56 00 61 00 72 00 46 00 69 00 6C 00 65 00 
        49 00 6E 00 66 00 6F 00 00 00 00 00 24 00 04 00 
        00 00 54 00 72 00 61 00 6E 00 73 00 6C 00 61 00 
        74 00 69 00 6F 00 6E 00 00 00 00 00 09 04 B0 04 
        00 00 00 00 
    """.trimIndent().replace(Regex("\\s+"), "").hexToByteArray()

    @Test
    fun testReadData() {
        val versionInfo = parseVersionData(data, 0)
        println(versionInfo)
    }
}