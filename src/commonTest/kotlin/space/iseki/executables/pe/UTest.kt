package space.iseki.executables.pe

import kotlin.test.Test
import kotlin.test.assertEquals

class UTest {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    //@Ignore // todo: might be a bug in the compiler
    fun testWString() {
        val data = "67 00 68 00 74 00 20 00 A9 00 20 00 32 00 30 00".replace(" ", "").hexToByteArray()
        val s = data.getWString(0, data.size)
        val expected = "ght © 20"
        println("actual: $s ${s.encodeToByteArray().toHexString()}")
        println("expected: $expected ${expected.encodeToByteArray().toHexString()}")
        println(s == expected)

        for ((index, c) in s.withIndex()) {
            println("$index: $c ${c.code} <> ${expected[index]} ${expected[index].code}")
        }
        assertEquals(expected, s)
    }
}