package space.iseki.executables.common

/**
 * Detects the type of the executable file from the given path.
 *
 * Note: The behavior of this method may vary across different platforms,
 * depending on how file paths are resolved and accessed.
 * @param path The path to the file
 * @return the detected [FileFormat], or `null` if the type could not be detected
 */
actual fun FileFormat.Companion.detect(path: String): FileFormat<OpenedFile>? {
    throw UnsupportedOperationException("Not yet implemented")
}

/**
 * Detects the file format and opens the file using the appropriate parser.
 *
 * This function first attempts to detect the file format from the file at the given path,
 * and then opens the file using the detected format's [FileFormat.open] method.
 *
 * Note: The behavior of this method may vary across different platforms,
 * depending on how file paths are resolved and accessed.
 * @param path The path to the file
 * @return A new file instance if the format is detected and parsing succeeds, or `null` if the format cannot be detected
 * @throws CommonFileException if the file format is invalid or unsupported (after successful detection)
 * @throws IOException if an I/O error occurs
 * @throws UnsupportedOperationException if the platform does not support file access
 */
@Throws(IOException::class)
actual fun FileFormat.Companion.open(path: String): OpenedFile? {
    throw UnsupportedOperationException("Not yet implemented")
}