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