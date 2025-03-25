package space.iseki.executables.common

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

internal class PathDataAccessor(private val path: Path) :
    SeekableByteChannelDataAccessor(Files.newByteChannel(path, StandardOpenOption.READ)) {
    override fun toString(): String = "PathDataAccessor(path=$path)"
}
