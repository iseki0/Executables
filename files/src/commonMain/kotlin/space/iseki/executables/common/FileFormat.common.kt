package space.iseki.executables.common

import space.iseki.executables.elf.ElfFile
import space.iseki.executables.macho.MachoFile
import space.iseki.executables.pe.PEFile

/**
 * Represents a object or executable file format.
 */
@Suppress("RedundantModalityModifier")
expect interface FileFormat<out T : OpenedFile> {
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
    open fun open(path: String): T

    companion object
}

/**
 * Detects the type of the executable file from the provided [DataAccessor].
 *
 * @param dataAccessor the data accessor to read the file header.
 * @throws IOException if an I/O error occurs
 * @return the detected [FileFormat], or `null` if the type could not be detected
 */
fun FileFormat.Companion.detect(dataAccessor: DataAccessor): FileFormat<OpenedFile>? {
    val buf = ByteArray(4)
    val read = dataAccessor.readAtMost(0, buf, 0, 4)
    if (read < 4) {
        return null
    }
    return when {
        buf[0] == 0x4d.toByte() && buf[1] == 0x5a.toByte() -> PEFile
        buf[0] == 0x7f.toByte() && buf[1] == 0x45.toByte() && buf[2] == 0x4c.toByte() && buf[3] == 0x46.toByte() -> ElfFile

        // 32-bit little endian
        buf[0] == 0xfe.toByte() && buf[1] == 0xed.toByte() && buf[2] == 0xfa.toByte() && buf[3] == 0xce.toByte() -> MachoFile
        // 32-bit big endian
        buf[0] == 0xce.toByte() && buf[1] == 0xfa.toByte() && buf[2] == 0xed.toByte() && buf[3] == 0xfe.toByte() -> MachoFile
        // 64-bit little endian
        buf[0] == 0xfe.toByte() && buf[1] == 0xed.toByte() && buf[2] == 0xfa.toByte() && buf[3] == 0xcf.toByte() -> MachoFile
        // 64-bit big endian
        buf[0] == 0xcf.toByte() && buf[1] == 0xfa.toByte() && buf[2] == 0xed.toByte() && buf[3] == 0xfe.toByte() -> MachoFile
        else -> null
    }
}

/**
 * Detects the file format and opens the file using the appropriate parser.
 *
 * This function first attempts to detect the file format using the provided [DataAccessor],
 * and then opens the file using the detected format's [FileFormat.open] method.
 *
 * @param accessor The data accessor that provides access to the file content
 * @return A new file instance if the format is detected and parsing succeeds, or `null` if the format cannot be detected
 * @throws CommonFileException if the file format is invalid or unsupported (after successful detection)
 * @throws IOException if an I/O error occurs
 */
@Throws(IOException::class)
fun FileFormat.Companion.open(accessor: DataAccessor): OpenedFile? {
    val detectedFormat = detect(accessor)
    return detectedFormat?.open(accessor)
}

/**
 * Detects the file format and opens the file using the appropriate parser.
 *
 * This function first attempts to detect the file format from the provided [ByteArray],
 * and then opens the file using the detected format's [FileFormat.open] method.
 *
 * @param bytes The bytes that represent the file content
 * @return A new file instance if the format is detected and parsing succeeds, or `null` if the format cannot be detected
 * @throws CommonFileException if the file format is invalid or unsupported (after successful detection)
 */
fun FileFormat.Companion.open(bytes: ByteArray): OpenedFile? {
    val detectedFormat = detect(bytes)
    return detectedFormat?.open(bytes)
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
expect fun FileFormat.Companion.open(path: String): OpenedFile?

fun FileFormat(name: String) = when (name) {
    "ELF" -> ElfFile
    "Mach-O" -> MachoFile
    "PE" -> PEFile
    else -> throw IllegalArgumentException("Unknown executable file type: $name")
}

/**
 * Detects the type of the executable file from the provided [ByteArray].
 *
 * @param data the data to read the file header.
 * @return the detected [FileFormat], or `null` if the type could not be detected
 */
fun FileFormat.Companion.detect(data: ByteArray): FileFormat<OpenedFile>? = detect(ByteArrayDataAccessor(data))

/**
 * Detects the type of the executable file from the given path.
 *
 * Note: The behavior of this method may vary across different platforms,
 * depending on how file paths are resolved and accessed.
 * @param path The path to the file
 * @return the detected [FileFormat], or `null` if the type could not be detected
 */
expect fun FileFormat.Companion.detect(path: String): FileFormat<OpenedFile>?

/**
 * Returns `true` if this is an ELF file.
 *
 * @return `true` if this is an ELF file, `false` otherwise
 */
fun FileFormat<*>.isElf() = this == ElfFile

/**
 * Returns `true` if this is a Mach-O file.
 *
 * @return `true` if this is a Mach-O file, `false` otherwise
 */
fun FileFormat<*>.isMacho() = this == MachoFile

/**
 * Returns `true` if this is a PE file.
 *
 * @return `true` if this is a PE file, `false` otherwise
 */
fun FileFormat<*>.isPE() = this == PEFile
