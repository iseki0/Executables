package space.iseki.executables.elf

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TestSerialization {

    @Test
    fun testELF32Half() {
        @Serializable
        data class A(val a: Elf32Half)

        val original = A(Elf32Half(0u))
        val json = Json.encodeToString(original)
        println("testELF32Half: " + json)
        val decoded = Json.decodeFromString<A>(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testELF32Word() {
        @Serializable
        data class Test(val value: Elf32Word)

        val original = Test(Elf32Word(123u))
        val json = Json.encodeToString(original)
        println("testELF32Word: " + json)
        val decoded = Json.decodeFromString<Test>(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testELF32Sword() {
        @Serializable
        data class Test(val value: Elf32Sword)

        val original = Test(Elf32Sword(-456))
        val json = Json.encodeToString(original)
        println("testELF32Sword: " + json)
        val decoded = Json.decodeFromString<Test>(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testELF32Addr() {
        @Serializable
        data class Test(val addr: Elf32Addr)

        val original = Test(Elf32Addr(0x1234u))
        val json = Json.encodeToString(original)
        println("testELF32Addr: " + json)
        val decoded = Json.decodeFromString<Test>(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testELF32Off() {
        @Serializable
        data class Test(val off: Elf32Off)

        val original = Test(Elf32Off(789u))
        val json = Json.encodeToString(original)
        println("testELF32Off: " + json)
        val decoded = Json.decodeFromString<Test>(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testELF64Half() {
        @Serializable
        data class Test(val value: Elf64Half)

        val original = Test(Elf64Half(100u))
        val json = Json.encodeToString(original)
        println("testELF64Half: " + json)
        val decoded = Json.decodeFromString<Test>(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testELF64Word() {
        @Serializable
        data class Test(val value: Elf64Word)

        val original = Test(Elf64Word(200u))
        val json = Json.encodeToString(original)
        println("testELF64Word: " + json)
        val decoded = Json.decodeFromString<Test>(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testELF64Xword() {
        @Serializable
        data class Test(val value: Elf64Xword)

        val original = Test(Elf64Xword(300UL))
        val json = Json.encodeToString(original)
        println("testELF64Xword: " + json)
        val decoded = Json.decodeFromString<Test>(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testELF64Sxword() {
        @Serializable
        data class Test(val value: Elf64Sxword)

        val original = Test(Elf64Sxword(-400L))
        val json = Json.encodeToString(original)
        println("testELF64Sxword: " + json)
        val decoded = Json.decodeFromString<Test>(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testELF64Addr() {
        @Serializable
        data class Test(val addr: Elf64Addr)

        val original = Test(Elf64Addr(0xDEADBEEFUL))
        val json = Json.encodeToString(original)
        println("testELF64Addr: " + json)
        val decoded = Json.decodeFromString<Test>(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testELF64Off() {
        @Serializable
        data class Test(val off: Elf64Off)

        val original = Test(Elf64Off(500UL))
        val json = Json.encodeToString(original)
        println("testELF64Off: " + json)
        val decoded = Json.decodeFromString<Test>(json)
        assertEquals(original, decoded)
    }
}