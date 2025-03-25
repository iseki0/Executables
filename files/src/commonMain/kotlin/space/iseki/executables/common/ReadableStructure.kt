package space.iseki.executables.common

/**
 * Provides a way to read a simple structure object
 */
interface ReadableStructure {
    /**
     * Get all fields of the structure
     *
     * @return a map of field names to their values
     */
    val fields: Map<String, Any>

}
