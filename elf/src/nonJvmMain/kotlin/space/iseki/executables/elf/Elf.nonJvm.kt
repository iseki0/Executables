@file:JvmName("ElfFiles")

package space.iseki.executables.elf

import space.iseki.executables.common.ByteArrayDataAccessor
import kotlin.jvm.JvmName

/**
 * Open an ELF file from the given bytes.
 *
 * @param bytes the bytes of the ELF file
 * @return the ELF file
 * @throws ElfFileException if the ELF file is invalid
 */
actual fun ElfFile(bytes: ByteArray): ElfFile = ElfFile.open(ByteArrayDataAccessor(bytes))
