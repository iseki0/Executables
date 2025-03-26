package space.iseki.executables.common

/**
 * Represents an I/O exception.
 *
 * @param message the detail message, or null if no detail message is provided.
 * @param cause the cause of this exception, or null if no cause is specified.
 */
expect open class IOException(message: String?, cause: Throwable?) : Exception {
    constructor(message: String?)
    constructor(cause: Throwable?)
}

/**
 * Represents an end-of-file exception.
 *
 * @param message the detail message, or null if no detail message is provided.
 */
expect class EOFException(message: String?) : IOException
