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

expect class NoSuchFileException : IOException {
    constructor(file: String?)
    constructor(file: String?, other: String?, reason: String?)
}

expect class AccessDeniedException : IOException {
    constructor(file: String?)
    constructor(file: String?, other: String?, reason: String?)
}

internal class CStringReadingException(val offset: Long, val reason: Reason) : RuntimeException() {
    constructor(offset: Int, reason: Reason) : this(offset.toUInt().toLong(), reason)

    enum class Reason {
        NULL_TERMINATOR, INVALID_CHARACTER,
    }

    override val message: String
        get() = "Failed to read C string at offset $offset, reason: $reason"
}

/**
 * Base exception for all file format processing errors.
 *
 * This exception provides structured error reporting by separating the static error message
 * from contextual parameters. The message is a fixed string constant that describes the
 * general type of error, while the arguments contain specific runtime values that
 * caused the error.
 *
 * @param message A fixed string constant describing the type of error
 * @param arguments A list of key-value pairs containing contextual information about the error.
 *                  Each pair consists of a parameter name and its string representation.
 * @param cause The underlying cause of this exception, if any
 */
open class CommonFileException internal constructor(
    message: String,
    val arguments: List<Pair<String, String>>,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

