package space.iseki.executables.pe

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsonTest {

    @Test
    fun testCharacteristicsJson() {
        val a = Characteristics.IMAGE_FILE_EXECUTABLE_IMAGE + Characteristics.IMAGE_FILE_DLL
        val jsonText = Json.encodeToString(a)
        println(jsonText)
        val b = Json.decodeFromString<Characteristics>(jsonText)
        assertEquals(a, b)
    }

    @Test
    fun testCharacteristicsFail() {
        assertFailsWith<SerializationException> {
            Json.decodeFromString<Characteristics>("""["A"]""")
        }.printStackTrace()
    }
}