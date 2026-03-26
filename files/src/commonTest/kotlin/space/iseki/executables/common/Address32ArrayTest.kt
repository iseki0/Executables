package space.iseki.executables.common

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class Address32ArrayTest {

    @Test
    fun testConstructionIndexingAndIteration() {
        val array = Address32Array(3) { index -> (index.toUInt() + 1u).toAddr() }

        assertEquals(3, array.size)
        assertFalse(array.isEmpty())
        assertEquals(Address32(1u), array[0])
        assertEquals(Address32(3u), array[2])
        assertContentEquals(listOf(Address32(1u), Address32(2u), Address32(3u)), array.toList())
    }

    @Test
    fun testContainsAndContainsAll() {
        val array = Address32Array(3) { index -> (index.toUInt() + 1u).toAddr() }

        assertTrue(Address32(2u) in array)
        assertFalse(Address32(4u) in array)
        assertTrue(array.containsAll(listOf(Address32(1u), Address32(3u))))
        assertFalse(array.containsAll(listOf(Address32(1u), Address32(4u))))

        val other = Address32Array(2) { index -> if (index == 0) Address32(1u) else Address32(3u) }
        assertTrue(array.containsAll(other))
    }

    @Test
    fun testEmptyArrayBehavior() {
        val array = Address32Array(0) { error("init should not be called") }

        assertEquals(0, array.size)
        assertTrue(array.isEmpty())
        assertContentEquals(emptyList(), array.toList())
        assertFalse(Address32(0u) in array)
        assertFailsWith<IndexOutOfBoundsException> { array[0] }
    }
}
