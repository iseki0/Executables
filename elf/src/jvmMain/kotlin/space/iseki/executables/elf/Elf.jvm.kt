@file:JvmName("ElfFiles")

package space.iseki.executables.elf

import space.iseki.executables.common.ByteArrayDataAccessor
import space.iseki.executables.common.IOException
import space.iseki.executables.common.SeekableByteChannelDataAccessor
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path


/**
 * Open an ELF file from the given bytes.
 *
 * @param bytes the bytes of the ELF file
 * @return the ELF file
 * @throws ElfFileException if the ELF file is invalid
 */
actual fun ElfFile(bytes: ByteArray): ElfFile = ElfFile.open(ByteArrayDataAccessor(bytes))

/**
 * Open an ELF file from the given path.
 *
 * @param path the path to the ELF file
 * @return the ELF file
 * @throws ElfFileException if the ELF file is invalid
 * @throws IOException if an I/O error occurs
 */
fun ElfFile(path: Path): ElfFile {
    val channel = Files.newByteChannel(path)
    try {
        val accessor = SeekableByteChannelDataAccessor(channel)
        return ElfFile.open(accessor)
    } catch (e: Throwable) {
        try {
            channel.close()
        } catch (e2: Throwable) {
            e.addSuppressed(e2)
        }
        throw e
    }
}

/**
 * Open an ELF file from the given file.
 *
 * @param file the file to the ELF file
 * @return the ELF file
 * @throws ElfFileException if the ELF file is invalid
 * @throws IOException if an I/O error occurs
 * @see File.toPath
 */
fun ElfFile(file: File): ElfFile {
    val raf = RandomAccessFile(file, "r")
    try {
        val accessor = SeekableByteChannelDataAccessor(raf.channel)
        return ElfFile.open(accessor)
    } catch (e: Throwable) {
        try {
            raf.close()
        } catch (e2: Throwable) {
            e.addSuppressed(e2)
        }
        throw e
    }
}
