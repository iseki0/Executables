package space.iseki.executables.common

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
     * Note: The behavior of this method may vary across different platforms,
     * depending on how file paths are resolved and accessed.
     * @param path The path to the file
     * @return A new file instance
     * @throws CommonFileException if the file format is invalid or unsupported
     * @throws IOException if an I/O error occurs
     * @throws UnsupportedOperationException if the platform does not support file access
     */
    actual fun open(path: String): T =
        throw UnsupportedOperationException("File access is not supported on this platform")

    actual companion object
}
