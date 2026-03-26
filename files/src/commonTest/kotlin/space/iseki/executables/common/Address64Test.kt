package space.iseki.executables.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Address64Test {

    @Test
    fun testToStringUsesFixedWidthHex() {
        assertEquals("0x0000000000000000", Address64(0UL).toString())
        assertEquals("0x0000000000000001", Address64(1UL).toString())
        assertEquals("0xffffffffffffffff", Address64(ULong.MAX_VALUE).toString())
    }

    @Test
    fun testAdditionIntRequiresNonNegativeOffset() {
        val base = Address64(0)
        assertFailsWith<IllegalArgumentException> { base + (-1) }
    }

    @Test
    fun testAdditionLongRequiresNonNegativeOffset() {
        val base = Address64(0)
        assertFailsWith<IllegalArgumentException> { base + (-1L) }
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
    fun testSubtractionRejectsNegativeSignedOffsets() {
        val base = Address64(1)
        assertFailsWith<IllegalArgumentException> { base - (-1) }
        assertFailsWith<IllegalArgumentException> { base - (-1L) }
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
    fun testModuloRequiresPositiveDivisor() {
        val base = Address64(0x1234UL)
        assertFailsWith<IllegalArgumentException> { base % 0 }
        assertFailsWith<IllegalArgumentException> { base % 0L }
        assertFailsWith<IllegalArgumentException> { base % 0u }
        assertFailsWith<IllegalArgumentException> { base % 0UL }
        assertFailsWith<IllegalArgumentException> { base % -1 }
        assertFailsWith<IllegalArgumentException> { base % -1L }
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
    fun testAlignRejectsZeroAlignment() {
        val addr = Address64(0x12345UL)
        assertFailsWith<IllegalArgumentException> { addr.isAlignedTo(0UL) }
        assertFailsWith<IllegalArgumentException> { addr.alignUp(0UL) }
        assertFailsWith<IllegalArgumentException> { addr.alignDown(0UL) }
    }

    @Test
    fun testToLongAndULong() {
        val addr = Address64(0x12345678UL)
        assertEquals(0x12345678UL, addr.toULong())
        assertEquals(0x12345678UL.toLong(), addr.toLong())
    }

    @Test
    fun testDistanceBetweenAddresses() {
        val start = Address64(0x1000UL)
        val end = Address64(0x1200UL)
        assertEquals(0x200UL, end - start)
    }

    @Test
    fun testDistanceWrapsAroundAsUnsigned() {
        val start = Address64(0x1200UL)
        val end = Address64(0x1000UL)
        assertEquals(ULong.MAX_VALUE - 0x1ffUL, end - start)
    }
}
