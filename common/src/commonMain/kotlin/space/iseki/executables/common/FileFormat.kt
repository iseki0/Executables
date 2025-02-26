package space.iseki.executables.common

/**
 * Represents a object or executable file format.
 */
@Suppress("RedundantModalityModifier")
expect interface FileFormat<T : OpenedFile> {
    /**
     * Opens and parses a file from the given data accessor.
     *
     * @param accessor The data accessor that provides access to the file content
     * @return A new file instance
     * @throws CommonFileException if the file format is invalid or unsupported
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun open(accessor: DataAccessor): T

    /**
     * Opens and parses a file from the given bytes.
     *
     * @param bytes The bytes that represent the file content
     * @return A new file instance
     * @throws CommonFileException if the file format is invalid or unsupported
     */
    open fun open(bytes: ByteArray): T
}

