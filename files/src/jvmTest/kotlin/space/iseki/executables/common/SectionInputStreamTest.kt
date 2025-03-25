package space.iseki.executables.common

import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class SectionInputStreamTest {

    /**
     * A mock implementation of ReadableSection for testing purposes.
     */
    private class MockSection(private val data: ByteArray) : ReadableSection {
        override val size: Long = data.size.toLong()
        override val name: String = "MockSection"

        override fun readBytes(sectionOffset: Long, buf: ByteArray, bufOffset: Int, size: Int) {
            if (sectionOffset < 0) {
                throw IllegalArgumentException("Section offset cannot be negative")
            }
            if (bufOffset < 0 || bufOffset + size > buf.size) {
                throw IndexOutOfBoundsException("Buffer offset or size out of bounds")
            }

            val availableBytes = maxOf(0L, this.size - sectionOffset)
            val bytesToCopy = minOf(availableBytes, size.toLong()).toInt()

            if (bytesToCopy > 0) {
                System.arraycopy(data, sectionOffset.toInt(), buf, bufOffset, bytesToCopy)
            }
        }
    }

    private fun ReadableSection.createInputStream(): InputStream {
        return SectionInputStream(this)
    }

    @Test
    fun testReadSingleByte() {
        // Create test data
        val testData = byteArrayOf(1, 2, 3, 4, 5)
        val section = MockSection(testData)
        val inputStream = section.createInputStream()

        // Read bytes one by one
        for (i in testData.indices) {
            val byte = inputStream.read()
            assertEquals(testData[i].toInt() and 0xFF, byte)
        }

        // Should return -1 at end of stream
        assertEquals(-1, inputStream.read())
    }

    @Test
    fun testReadMultipleBytes() {
        // Create test data
        val testData = ByteArray(1024) { it.toByte() }
        val section = MockSection(testData)
        val inputStream = section.createInputStream()

        // Read in chunks
        val buffer = ByteArray(256)
        var totalBytesRead = 0
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            for (i in 0 until bytesRead) {
                assertEquals(testData[totalBytesRead + i], buffer[i])
            }
            totalBytesRead += bytesRead
        }

        assertEquals(testData.size, totalBytesRead)
    }

    @Test
    fun testSkip() {
        // Create test data
        val testData = ByteArray(1000) { it.toByte() }
        val section = MockSection(testData)
        val inputStream = section.createInputStream()

        // Skip some bytes
        val bytesToSkip = 500L
        val skipped = inputStream.skip(bytesToSkip)
        assertEquals(bytesToSkip, skipped)

        // Read after skipping
        val byte = inputStream.read()
        assertEquals(testData[500].toInt() and 0xFF, byte)
    }

    @Test
    fun testAvailable() {
        // Create test data
        val testData = ByteArray(1000) { it.toByte() }
        val section = MockSection(testData)
        val inputStream = section.createInputStream()

        // Check initial available
        assertEquals(1000, inputStream.available())

        // Read some bytes
        val buffer = ByteArray(300)
        inputStream.read(buffer)

        // Check available after reading
        assertEquals(700, inputStream.available())
    }

    @Test
    fun testReadWithOffset() {
        // Create test data
        val testData = ByteArray(100) { it.toByte() }
        val section = MockSection(testData)
        val inputStream = section.createInputStream()

        // Read with offset
        val buffer = ByteArray(50)
        val bytesRead = inputStream.read(buffer, 10, 30)

        assertEquals(30, bytesRead)

        // Verify first 10 bytes are untouched (should be 0)
        for (i in 0 until 10) {
            assertEquals(0, buffer[i])
        }

        // Verify next 30 bytes match the data
        for (i in 0 until 30) {
            assertEquals(testData[i], buffer[i + 10])
        }

        // Verify last 10 bytes are untouched (should be 0)
        for (i in 40 until 50) {
            assertEquals(0, buffer[i])
        }
    }

    @Test
    fun testReadZeroBytes() {
        // Create test data
        val testData = byteArrayOf(1, 2, 3, 4, 5)
        val section = MockSection(testData)
        val inputStream = section.createInputStream()

        // Read zero bytes
        val buffer = ByteArray(10)
        val bytesRead = inputStream.read(buffer, 0, 0)

        assertEquals(0, bytesRead)

        // Position should not have changed
        assertEquals(5, inputStream.available())
    }
} 