package space.iseki.executables.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Address32Test {

    @Test
    fun testToStringUsesFixedWidthHex() {
        assertEquals("0x00000000", Address32(0u).toString())
        assertEquals("0x00000001", Address32(1u).toString())
        assertEquals("0xffffffff", Address32(UInt.MAX_VALUE).toString())
    }

    @Test
    fun testAdditionIntRequiresNonNegativeOffset() {
        val base = Address32(0)
        assertFailsWith<IllegalArgumentException> { base + (-1) }
    }

    @Test
    fun testSubtractionIntRequiresNonNegativeOffset() {
        val base = Address32(0)
        val result = base - 1
        assertEquals("0xffffffff", result.toString())
    }

    @Test
    fun testSubtractionIntRejectsNegativeOffset() {
        val base = Address32(1)
        assertFailsWith<IllegalArgumentException> { base - (-1) }
    }

    @Test
    fun testAdditionUInt() {
        val base = Address32(1)
        val result = base + 0xFFFFFFFFu
        assertEquals("0x00000000", result.toString()) // 1 + 0xFFFFFFFF = 0x100000000 → wrap to 0x0
    }

    @Test
    fun testSubtractionUInt() {
        val base = Address32(0)
        val result = base - 0x1u
        assertEquals("0xffffffff", result.toString())
    }

    @Test
    fun testModuloAllTypes() {
        val base = Address32(0x1234u)
        assertEquals("0x00000234", (base % 0x1000).toString())
        assertEquals("0x00000234", (base % 0x1000u).toString())
    }

    @Test
    fun testModuloRequiresPositiveDivisor() {
        val base = Address32(0x1234u)
        assertFailsWith<IllegalArgumentException> { base % 0 }
        assertFailsWith<IllegalArgumentException> { base % 0u }
        assertFailsWith<IllegalArgumentException> { base % -1 }
    }

    @Test
    fun testBitwiseOrAllTypes() {
        val base = Address32(0x0000u)
        assertEquals("0x00001234", (base or 0x1234).toString())
        assertEquals("0x00001234", (base or 0x1234u).toString())
    }

    @Test
    fun testBitwiseAndAllTypes() {
        val base = Address32(0xFFFFu)
        assertEquals("0x00001234", (base and 0x1234).toString())
        assertEquals("0x00001234", (base and 0x1234u).toString())
    }

    @Test
    fun testBitwiseXorAllTypes() {
        val base = Address32(0xAAAAu)
        assertEquals("0x0000ffff", (base xor 0x5555).toString())
        assertEquals("0x0000ffff", (base xor 0x5555u).toString())
    }

    @Test
    fun testBitwiseInv() {
        val result = Address32(0x0u).inv()
        assertEquals("0xffffffff", result.toString())
    }

    @Test
    fun testShifts() {
        val base = Address32(1)
        assertEquals("0x00010000", (base shl 16).toString())
        assertEquals("0x00000001", ((base shl 16) shr 16).toString())
    }

    @Test
    fun testAlign() {
        val addr = Address32(0x12345u)
        assertEquals("0x00013000", addr.alignUp(0x1000u).toString())
        assertEquals("0x00012000", addr.alignDown(0x1000u).toString())
    }

    @Test
    fun testAlignRejectsZeroAlignment() {
        val addr = Address32(0x12345u)
        assertFailsWith<IllegalArgumentException> { addr.isAlignedTo(0u) }
        assertFailsWith<IllegalArgumentException> { addr.alignUp(0u) }
        assertFailsWith<IllegalArgumentException> { addr.alignDown(0u) }
    }

    @Test
    fun testToIntAndUInt() {
        val addr = Address32(0x12345678u)
        assertEquals(0x12345678u, addr.toUInt())
        assertEquals(0x12345678, addr.toInt())
    }

    @Test
    fun testDistanceBetweenAddresses() {
        val start = Address32(0x1000u)
        val end = Address32(0x1200u)
        assertEquals(0x200u, end - start)
    }

    @Test
    fun testDistanceWrapsAroundAsUnsigned() {
        val start = Address32(0x1200u)
        val end = Address32(0x1000u)
        assertEquals(UInt.MAX_VALUE - 0x1ffu, end - start)
    }
}
