package space.iseki.executables.pe

import kotlin.jvm.JvmOverloads

/**
 * Represents an exception occurring during PE file processing.
 *
 * @param message the detailed message describing the error
 * @param cause the underlying reason for the exception
 */
open class PEFileException @JvmOverloads constructor(message: String, override val cause: Throwable? = null) :
    RuntimeException(message, cause)
