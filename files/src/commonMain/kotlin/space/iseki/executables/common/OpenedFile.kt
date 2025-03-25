package space.iseki.executables.common

interface OpenedFile : AutoCloseable {
    /**
     * The root headers of the file.
     */
    val rootHeaders: Map<String, ReadableStructure> get() = emptyMap()
}
