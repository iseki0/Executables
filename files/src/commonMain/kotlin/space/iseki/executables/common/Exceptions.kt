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

open class CommonFileException internal constructor(
    message: String,
    val arguments: List<Pair<String, String>>,
    cause: Throwable? = null,
) : RuntimeException(cause) {
    override val message: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val args = if (arguments.isEmpty()) "" else arguments.joinToString(", ") { "${it.first}=${it.second}" }
        if (args.isNotEmpty()) "$message ($args)" else message.ifEmpty { "An error occurred" }
    }
}

