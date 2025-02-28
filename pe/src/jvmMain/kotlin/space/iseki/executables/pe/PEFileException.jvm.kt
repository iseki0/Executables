package space.iseki.executables.pe

import space.iseki.executables.common.CommonFileException
import kotlin.jvm.JvmOverloads

/**
 * JVM implementation of [PEFileException].
 *
 * @param message the detailed message describing the error
 * @param cause the underlying reason for the exception
 */
actual open class PEFileException @JvmOverloads actual constructor(
    message: String,
    override val cause: Throwable?
) : CommonFileException(message, cause)
