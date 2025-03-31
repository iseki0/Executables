package space.iseki.executables.common

import space.iseki.executables.pe.PEFile
import kotlin.test.Test
import kotlin.test.assertNotNull

class NotepadTest {
    @Test
    fun testOpenNotepad() {
        PEFile.open("C:/Windows/system32/notepad.exe").use { peFile ->
            println(peFile.summary)
            val versionInfo = peFile.versionInfo
            assertNotNull(versionInfo)
            println(versionInfo)
        }
    }

}