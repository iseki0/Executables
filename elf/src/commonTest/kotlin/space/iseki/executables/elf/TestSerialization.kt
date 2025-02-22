package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test

class TestSerialization {

    @Test
    fun testELF32Half() {
        @Serializable
        data class A(val a: Elf32Half)

        val t = Json.encodeToString(A(Elf32Half(0u))).let { println(it) }
    }
}