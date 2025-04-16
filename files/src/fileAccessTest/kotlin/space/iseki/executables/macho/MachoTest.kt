package space.iseki.executables.macho

import kotlinx.serialization.json.Json
import space.iseki.executables.common.openNativeFileDataAccessor
import space.iseki.executables.common.toAddr
import space.iseki.executables.macho.lc.SegmentCommand
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MachoTest {
    companion object {
        const val PATH = "src/fileAccessTest/resources/macho/tiny-macho"
    }

    @Test
    fun test() {
        // language=JSON
        val eH = """
            {"magic":"MH_CIGAM","cputype":7,"cpusubtype":3,"filetype":"MH_EXECUTE","ncmds":2,"sizeofcmds":136,"flags":["MH_PIE","MH_NO_HEAP_EXECUTION"],"isLittleEndian":true}
        """.trimIndent().let { Json.decodeFromString<MachoHeader>(it) }
        MachoFile.open(PATH).use {
            assertEquals(eH, it.header)
        }
    }

    @Test
    fun testReadCommands() {
        MachoFile.open(PATH).use {
            val segmentCommand = it.loaderCommands[1] as SegmentCommand
            assertEquals("LC_SEGMENT", segmentCommand.type.toString())
            assertContains(segmentCommand.segName, "SP1")
            assertEquals(0x00001000.toAddr(), segmentCommand.vmAddr)
            assertEquals(4096u, segmentCommand.vmSize)
            assertEquals(MachoVMProt.VM_PROT_EXECUTE + MachoVMProt.VM_PROT_READ, segmentCommand.initProt)
        }
    }

    @Test
    fun testReadCommand2() {
        MachoFile.open("src/fileAccessTest/resources/macho/clang-386-darwin-exec-with-rpath").use { file ->
            for (loaderCommand in file.loaderCommands) {
                println(loaderCommand)
            }
        }
    }

    @Test
    fun testReadSections() {
        val path = "src/fileAccessTest/resources/macho/clang-386-darwin-exec-with-rpath"
        MachoFile.open(path).use { file ->
            for (section in file.sections) {
                val dataFilename = "${section.header.segName}${section.name}"
                val expected = openNativeFileDataAccessor("$path.sections/$dataFilename").use { r ->
                    ByteArray(r.size.toInt()).also { r.readFully(0, it) }
                }
                val actual = ByteArray(section.size.toInt())
                section.readBytes(0, actual, 0, actual.size)
                assertEquals(
                    expected = expected.size,
                    actual = actual.size,
                    message = "Section $dataFilename size mismatch: expected ${expected.size} bytes, but got ${actual.size} bytes.",
                )
                assertContentEquals(
                    expected = expected,
                    actual = actual,
                    message = "Section $dataFilename mismatch: expected ${expected.size} bytes, but got ${actual.size} bytes.",
                )
            }
        }
    }
}
