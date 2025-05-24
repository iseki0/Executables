package space.iseki.executables.macho

import space.iseki.executables.common.CommonFileException

/**
 * Exception thrown when a Mach-O file is invalid or cannot be processed.
 */
class MachoFileException internal constructor(
    message: String,
    arguments: List<Pair<String, String>> = emptyList(),
    cause: Throwable? = null,
) : CommonFileException(message, arguments, cause) {
    internal constructor(
        message: String,
        cause: Throwable? = null,
    ) : this(message, emptyList(), cause)
}
