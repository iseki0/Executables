package space.iseki.executables.pe

import space.iseki.executables.common.CommonFileException

/**
 * Represents an exception occurring during PE file processing.
 *
 * @param message the detailed message describing the error
 * @param cause the underlying reason for the exception
 */
open class PEFileException(message: String, cause: Throwable? = null) : CommonFileException(message, cause)
