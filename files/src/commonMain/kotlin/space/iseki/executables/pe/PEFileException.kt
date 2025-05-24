package space.iseki.executables.pe

import space.iseki.executables.common.CommonFileException

/**
 * Represents an exception occurring during PE file processing.
 *
 * This exception provides structured error reporting by separating the static error message
 * from contextual parameters. The message should be a fixed string constant that describes the
 * general type of error, while the arguments contain specific runtime values that
 * caused the error.
 *
 * @param message A fixed string constant describing the type of error
 * @param arguments A list of key-value pairs containing contextual information about the error.
 *                  Each pair consists of a parameter name and its string representation.
 * @param cause The underlying cause of this exception, if any
 */
open class PEFileException internal constructor(
    message: String,
    arguments: List<Pair<String, String>> = emptyList(),
    cause: Throwable? = null,
) : CommonFileException(message, arguments, cause) {
    internal constructor(
        message: String,
        cause: Throwable? = null,
    ) : this(message, emptyList(), cause)
}
