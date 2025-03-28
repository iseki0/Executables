package space.iseki.executables.pe.vi

import space.iseki.executables.common.NoSuchFileException
import space.iseki.executables.pe.PEFile
import kotlin.test.Test
import kotlin.test.assertNotNull

class NotepadTest {
    @Test
    fun testOpenNotepad() {
        try {
            PEFile.open("C:/Windows/system32/notepad.exe").use { peFile ->
                println(peFile.summary)
                val versionInfo = peFile.versionInfo
                assertNotNull(versionInfo)
                println(versionInfo)
            }
        } catch (e: UnsupportedOperationException) {
        } catch (e: NoSuchFileException) {
        }
    }

}