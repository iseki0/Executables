package space.iseki.executables.common

import java.io.File
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Detects the type of the executable file from the provided [DataAccessor].
 *
 * The file will be opened in [StandardOpenOption.READ] mode.
 *
 * @param file the path to the file to be read.
 * @throws IOException if an I/O error occurs
 * @see Files.newByteChannel
 * @throws UnsupportedOperationException – if the [StandardOpenOption.READ] operation is not supported by the file system
 * @return the detected [ExecutableFile], or `null` if the type could not be detected
 */
@Throws(IOException::class)
fun ExecutableFile.Companion.detect(file: Path) =
    Files.newByteChannel(file, StandardOpenOption.READ).use { detect(SeekableByteChannelDataAccessor(it)) }

/**
 * Detects the type of the executable file from the provided [File].
 *
 * @param file the file to open.
 * @throws IOException if an I/O error occurs
 * @see detect(Path)
 * @throws UnsupportedOperationException if the [StandardOpenOption.READ] operation is not supported by the file system
 * @throws InvalidPathException if the path is invalid
 * @return the detected [ExecutableFile], or `null` if the type could not be detected
 */
@Throws(IOException::class)
fun ExecutableFile.Companion.detect(file: File) = detect(file.toPath())
