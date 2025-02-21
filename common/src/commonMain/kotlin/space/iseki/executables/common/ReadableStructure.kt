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

    /**
     * Get the total number of fields in the structure
     *
     * @return the total number of fields, returns -1 if unknown or variable
     */
    val totalFields: Int
        get() = -1
}
