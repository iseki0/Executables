package space.iseki.executables.common

import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SeekableByteChannelDataAccessorTest {
    @Test
    fun readAtMostIsSafeForConcurrentReadsOnSharedChannel() {
        withPatternFile { tempFile ->
            SlowSeekableByteChannel(Files.newByteChannel(tempFile, StandardOpenOption.READ)).use { channel ->
                exerciseConcurrentReads(SeekableByteChannelDataAccessor(channel))
            }
        }
    }

    @Test
    fun readAtMostIsSafeForConcurrentReadsOnSharedRandomAccessFile() {
        withPatternFile { tempFile ->
            RandomAccessFile(tempFile.toFile(), "r").use { raf ->
                exerciseConcurrentReads(RandomAccessFileDataAccessor(raf))
            }
        }
    }

    private fun exerciseConcurrentReads(accessor: DataAccessor) {
        val executor = Executors.newFixedThreadPool(2)
        try {
            val firstTask = Callable {
                repeat(1_000) {
                    val buffer = ByteArray(128)
                    val read = accessor.readAtMost(0, buffer, 0, buffer.size)
                    assertContentEquals(ByteArray(128) { 'A'.code.toByte() }, buffer)
                    assertEquals(buffer.size, read)
                }
            }
            val secondTask = Callable {
                repeat(1_000) {
                    val buffer = ByteArray(128)
                    val read = accessor.readAtMost(4096, buffer, 0, buffer.size)
                    assertContentEquals(ByteArray(128) { 'B'.code.toByte() }, buffer)
                    assertEquals(buffer.size, read)
                }
            }

            val futures = listOf(executor.submit(firstTask), executor.submit(secondTask))
            futures.forEach { it.get(30, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
            accessor.close()
        }
    }

    private inline fun withPatternFile(block: (java.nio.file.Path) -> Unit) {
        val tempFile = Files.createTempFile("executables-", ".bin")
        try {
            val firstRegion = ByteArray(4096) { 'A'.code.toByte() }
            val secondRegion = ByteArray(4096) { 'B'.code.toByte() }
            Files.write(tempFile, firstRegion + secondRegion)
            block(tempFile)
        } finally {
            Files.deleteIfExists(tempFile)
        }
    }

    private class SlowSeekableByteChannel(private val delegate: SeekableByteChannel) : SeekableByteChannel {
        override fun read(dst: ByteBuffer): Int = delegate.read(dst)

        override fun write(src: ByteBuffer): Int = delegate.write(src)

        override fun position(): Long = delegate.position()

        override fun position(newPosition: Long): SeekableByteChannel {
            delegate.position(newPosition)
            Thread.sleep(1)
            return this
        }

        override fun size(): Long = delegate.size()

        override fun truncate(size: Long): SeekableByteChannel = delegate.truncate(size)

        override fun isOpen(): Boolean = delegate.isOpen

        override fun close() {
            delegate.close()
        }
    }
}
