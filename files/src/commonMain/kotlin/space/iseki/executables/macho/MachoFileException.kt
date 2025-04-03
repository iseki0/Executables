package space.iseki.executables.macho

import space.iseki.executables.common.CommonFileException

/**
 * Exception thrown when a Mach-O file is invalid or cannot be processed.
 */
class MachoFileException : CommonFileException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
