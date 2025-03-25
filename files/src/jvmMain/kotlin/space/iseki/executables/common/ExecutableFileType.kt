package space.iseki.executables.common

import java.io.File
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 *  Detects the type of the executable file from the provided [File].
 *
 *  @param file the file to read the file header.
 *  @return the detected [ExecutableFileType], or `null` if the type could not be detected
 *  @throws IOException if an I/O error occurs
 *  @throws InvalidPathException if the path is invalid
 *  @see File.toPath
 */
fun ExecutableFileType.Companion.detect(file: File): ExecutableFileType? =
    ExecutableFileType.Companion.detect(file.toPath())

/**
 * Detects the type of the executable file from the provided [Path].
 *
 * @param path the path to read the file header.
 * @return the detected [ExecutableFileType], or `null` if the type could not be detected
 * @throws IOException if an I/O error occurs
 * @see Files.newByteChannel
 */
fun ExecutableFileType.Companion.detect(path: Path): ExecutableFileType? =
    Files.newByteChannel(path, StandardOpenOption.READ).use { detect(SeekableByteChannelDataAccessor(it)) }
