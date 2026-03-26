package space.iseki.executables.common

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class Address64ArrayTest {

    @Test
    fun testConstructionIndexingAndIteration() {
        val array = Address64Array(3) { index -> (index.toULong() + 1uL).toAddr() }

        assertEquals(3, array.size)
        assertFalse(array.isEmpty())
        assertEquals(Address64(1UL), array[0])
        assertEquals(Address64(3UL), array[2])
        assertContentEquals(listOf(Address64(1UL), Address64(2UL), Address64(3UL)), array.toList())
    }

    @Test
    fun testContainsAndContainsAll() {
        val array = Address64Array(3) { index -> (index.toULong() + 1uL).toAddr() }

        assertTrue(Address64(2UL) in array)
        assertFalse(Address64(4UL) in array)
        assertTrue(array.containsAll(listOf(Address64(1UL), Address64(3UL))))
        assertFalse(array.containsAll(listOf(Address64(1UL), Address64(4UL))))

        val other = Address64Array(2) { index -> if (index == 0) Address64(1UL) else Address64(3UL) }
        assertTrue(array.containsAll(other))
    }

    @Test
    fun testEmptyArrayBehavior() {
        val array = Address64Array(0) { error("init should not be called") }

        assertEquals(0, array.size)
        assertTrue(array.isEmpty())
        assertContentEquals(emptyList(), array.toList())
        assertFalse(Address64(0UL) in array)
        assertFailsWith<IndexOutOfBoundsException> { array[0] }
    }
}
