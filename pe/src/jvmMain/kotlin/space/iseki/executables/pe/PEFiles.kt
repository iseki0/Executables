@file:JvmName("PEFiles")

package space.iseki.executables.pe

import space.iseki.executables.common.SeekableByteChannelDataAccessor
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Open a PE file from a [SeekableByteChannel].
 *
 * @param channel the channel to read the PE file from
 * @param closeWhenTheFileClosed whether to close the channel when the PE file is closed
 * @return the PE file
 * @throws PEFileException if the file is not a valid PE file
 * @throws IOException if an I/O error occurs
 */
@JvmOverloads
@JvmName("open")
fun PEFile(channel: SeekableByteChannel, closeWhenTheFileClosed: Boolean = true): PEFile {
    val accessor = object : SeekableByteChannelDataAccessor(channel) {
        override fun close() {
            if (closeWhenTheFileClosed) {
                channel.close()
            }
        }
    }
    return PEFile.open(accessor)
}

/**
 * Open a PE file from a [File].
 *
 * @param file the file to read the PE file from
 * @return the PE file
 * @throws PEFileException if the file is not a valid PE file
 * @throws IOException if an I/O error occurs
 * @throws FileNotFoundException if the file does not exist
 */
@Throws(IOException::class)
@JvmName("open")
fun PEFile(file: File): PEFile {
    val input = file.inputStream()
    try {
        return PEFile(input.channel)
    } catch (th: Throwable) {
        try {
            input.close()
        } catch (th1: Throwable) {
            th.addSuppressed(th1)
        }
        throw th
    }
}

/**
 * Open a PE file from a [Path].
 *
 * @param path the path to read the PE file from
 * @return the PE file
 * @throws PEFileException if the file is not a valid PE file
 * @throws IOException if an I/O error occurs
 * @throws NoSuchFileException if the file does not exist
 */
@Throws(IOException::class)
@JvmName("open")
fun PEFile(path: Path): PEFile {
    val channel = Files.newByteChannel(path, StandardOpenOption.READ)
    try {
        return PEFile(channel)
    } catch (th: Throwable) {
        try {
            channel.close()
        } catch (th1: Throwable) {
            th.addSuppressed(th1)
        }
        throw th
    }
}
