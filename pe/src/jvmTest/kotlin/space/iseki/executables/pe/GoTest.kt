package space.iseki.executables.pe

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToJsonElement
import space.iseki.executables.common.ByteArrayDataAccessor
import space.iseki.executables.common.ExecutableFile
import kotlin.test.Test
import kotlin.test.assertEquals

private val classLoader = GoTest::class.java.classLoader
private val data = classLoader.getResourceAsStream("go.exe")!!.use { it.readAllBytes() }

@OptIn(ExperimentalSerializationApi::class)
private val summaryJson =
    classLoader.getResourceAsStream("go.exe.summary.json")!!.use { Json.decodeFromStream<JsonElement>(it) }
private val json = Json { prettyPrint = true }

class GoTest {

    @Test
    fun test() {
        assertEquals(ExecutableFile.PE, ExecutableFile.detect(ByteArrayDataAccessor(data)))
        PEFile.wrap(data).use { pe ->
            assertEquals(summaryJson, json.encodeToJsonElement(pe.summary))
            println(json.encodeToString(pe.versionInfo?.stringFileInfo?.strings))
        }
    }
}
