package space.iseki.executables.macho

import space.iseki.executables.common.IOException

/**
 * Exception thrown when a Mach-O file is invalid or cannot be processed.
 */
class MachoFileException : IOException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
