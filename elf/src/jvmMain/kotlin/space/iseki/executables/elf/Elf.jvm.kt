@file:JvmName("ElfFiles")

package space.iseki.executables.elf

import space.iseki.executables.common.ByteArrayDataAccessor
import space.iseki.executables.common.IOException
import space.iseki.executables.common.SeekableByteChannelDataAccessor
import java.io.File
import java.nio.file.Files
import java.nio.file.InvalidPathException
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
fun ElfFile(path: Path): ElfFile =
    Files.newByteChannel(path).let { ElfFile.open(SeekableByteChannelDataAccessor(it)) }

/**
 * Open an ELF file from the given file.
 *
 * @param file the file to the ELF file
 * @return the ELF file
 * @throws ElfFileException if the ELF file is invalid
 * @throws IOException if an I/O error occurs
 * @throws InvalidPathException if the path is invalid
 * @see File.toPath
 */
fun ElfFile(file: File): ElfFile = ElfFile(file.toPath())
