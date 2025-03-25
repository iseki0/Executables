package space.iseki.executables.common

import java.io.File
import java.io.FileNotFoundException
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.NoSuchFileException
import java.nio.file.Path

/**
 * Represents a object or executable file format.
 */
actual interface FileFormat<out T : OpenedFile> {
    /**
     * Opens and parses a file from the given data accessor.
     *
     * @param accessor The data accessor that provides access to the file content
     * @return A new file instance
     * @throws CommonFileException if the file format is invalid or unsupported
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    actual fun open(accessor: DataAccessor): T

    /**
     * Opens and parses a file from the given bytes.
     *
     * @param bytes The bytes that represent the file content
     * @return A new file instance
     * @throws CommonFileException if the file format is invalid or unsupported
     */
    actual fun open(bytes: ByteArray): T = open(ByteArrayDataAccessor(bytes))

    /**
     * Opens and parses a file from the given path.
     *
     * @param path The path to the file
     * @return A new file instance
     * @throws CommonFileException if the file format is invalid or unsupported
     * @throws IOException if an I/O error occurs
     * @throws NoSuchFileException if the file does not exist
     *
     * @see Files.newByteChannel
     */
    @Throws(IOException::class)
    fun open(path: Path): T {
        val dataAccessor = PathDataAccessor(path)
        try {
            return open(dataAccessor)
        } catch (e: Throwable) {
            runCatching { dataAccessor.close() }.onFailure { e.addSuppressed(it) }
            throw e
        }
    }

    /**
     * Opens and parses a file from the given file.
     *
     * @param file The file to open
     * @return A new file instance
     * @throws CommonFileException if the file format is invalid or unsupported
     * @throws IOException if an I/O error occurs
     * @throws FileNotFoundException if the file does not exist
     */
    @Throws(IOException::class)
    fun open(file: File): T {
        val dataAccessor = RandomAccessFileDataAccessor(file)
        try {
            return open(dataAccessor)
        } catch (e: Throwable) {
            runCatching { dataAccessor.close() }.onFailure { e.addSuppressed(it) }
            throw e
        }
    }

    /**
     * Opens and parses a file from the given channel.
     *
     * The channel will be closed after the file is closed.
     *
     * @param channel The channel to open
     * @return A new file instance
     * @throws CommonFileException if the file format is invalid or unsupported
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun open(channel: SeekableByteChannel): T {
        return open(SeekableByteChannelDataAccessor(channel))
    }

    /**
     * Opens and parses a file from the given path.
     *
     * Note: The behavior of this method may vary across different platforms,
     * depending on how file paths are resolved and accessed.
     * @param path The path to the file
     * @return A new file instance
     * @throws CommonFileException if the file format is invalid or unsupported
     * @throws IOException if an I/O error occurs
     * @throws UnsupportedOperationException if the platform does not support file access
     * @throws InvalidPathException if the path string cannot be converted to a Path
     * @throws NoSuchFileException if the file does not exist
     * @see Path.of
     */
    @Throws(IOException::class)
    actual fun open(path: String): T = open(Path.of(path))

    actual companion object
}

/**
 * Detects the type of the executable file from the given path.
 *
 * Note: The behavior of this method may vary across different platforms,
 * depending on how file paths are resolved and accessed.
 * @param path The path to the file
 * @return the detected [FileFormat], or `null` if the type could not be detected
 */
actual fun FileFormat.Companion.detect(path: String): FileFormat<OpenedFile>? {
    val da = PathDataAccessor(Path.of(path))
    try {
        return detect(da)
    } finally {
        da.close()
    }
}
