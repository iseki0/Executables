package space.iseki.executables.elf

import kotlinx.serialization.json.Json
import space.iseki.executables.common.ByteArrayDataAccessor
import space.iseki.executables.common.ExecutableFileType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val classLoader = GoTest::class.java.classLoader
private val data = classLoader.getResourceAsStream("hello")!!.use { it.readAllBytes() }
private val json = Json { prettyPrint = true }

class GoTest {
    @Test
    fun test() {
        assertNotNull(data)
        println("Go binary size: ${data.size} bytes")
        assertEquals(ExecutableFileType.ELF, ExecutableFileType.detect(ByteArrayDataAccessor(data)))
        ElfFile.open(ByteArrayDataAccessor(data))
    }
}
