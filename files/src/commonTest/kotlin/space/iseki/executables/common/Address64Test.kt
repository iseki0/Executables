package space.iseki.executables.common

import kotlin.test.Test
import kotlin.test.assertEquals

class Address64Test {

    @Test
    fun testWraparoundAdditionInt() {
        val base = Address64(0)
        val result = base + (-1)
        assertEquals("0xffffffffffffffff", result.toString())
    }

    @Test
    fun testWraparoundAdditionLong() {
        val base = Address64(0)
        val result = base + (-1L)
        assertEquals("0xffffffffffffffff", result.toString())
    }

    @Test
    fun testWraparoundSubtractionInt() {
        val base = Address64(0)
        val result = base - 1
        assertEquals("0xffffffffffffffff", result.toString())
    }

    @Test
    fun testWraparoundSubtractionLong() {
        val base = Address64(0)
        val result = base - 1L
        assertEquals("0xffffffffffffffff", result.toString())
    }

    @Test
    fun testAdditionUIntULong() {
        val base = Address64(1)
        val resultUInt = base + 0xFFFFFFFFu
        val resultULong = base + 0xFFFFFFFFUL
        assertEquals("0x0000000100000000", resultUInt.toString())
        assertEquals("0x0000000100000000", resultULong.toString())
    }

    @Test
    fun testSubtractionUIntULong() {
        val base = Address64(0)
        val resultUInt = base - 0x1u
        val resultULong = base - 0x1UL
        assertEquals("0xffffffffffffffff", resultUInt.toString())
        assertEquals("0xffffffffffffffff", resultULong.toString())
    }

    @Test
    fun testModuloAllTypes() {
        val base = Address64(0x1234UL)
        assertEquals("0x0000000000000234", (base % 0x1000).toString())
        assertEquals("0x0000000000000234", (base % 0x1000L).toString())
        assertEquals("0x0000000000000234", (base % 0x1000u).toString())
        assertEquals("0x0000000000000234", (base % 0x1000UL).toString())
    }

    @Test
    fun testBitwiseOrAllTypes() {
        val base = Address64(0x0000UL)
        assertEquals("0x0000000000001234", (base or 0x1234).toString())
        assertEquals("0x0000000000001234", (base or 0x1234L).toString())
        assertEquals("0x0000000000001234", (base or 0x1234UL).toString())
    }

    @Test
    fun testBitwiseAndAllTypes() {
        val base = Address64(0xFFFFUL)
        assertEquals("0x0000000000001234", (base and 0x1234).toString())
        assertEquals("0x0000000000001234", (base and 0x1234L).toString())
        assertEquals("0x0000000000001234", (base and 0x1234UL).toString())
    }

    @Test
    fun testBitwiseXorAllTypes() {
        val base = Address64(0xAAAAUL)
        assertEquals("0x000000000000ffff", (base xor 0x5555).toString())
        assertEquals("0x000000000000ffff", (base xor 0x5555L).toString())
        assertEquals("0x000000000000ffff", (base xor 0x5555UL).toString())
    }

    @Test
    fun testBitwiseInv() {
        val result = Address64(0x0UL).inv()
        assertEquals("0xffffffffffffffff", result.toString())
    }

    @Test
    fun testShifts() {
        val base = Address64(1)
        assertEquals("0x0000000100000000", (base shl 32).toString())
        assertEquals("0x0000000000000001", ((base shl 32) shr 32).toString())
    }

    @Test
    fun testAlign() {
        val addr = Address64(0x12345UL)
        assertEquals("0x0000000000013000", addr.alignUp(0x1000UL).toString())
        assertEquals("0x0000000000012000", addr.alignDown(0x1000UL).toString())
    }

    @Test
    fun testToLongAndULong() {
        val addr = Address64(0x12345678UL)
        assertEquals(0x12345678UL, addr.toULong())
        assertEquals(0x12345678UL.toLong(), addr.toLong())
    }
}