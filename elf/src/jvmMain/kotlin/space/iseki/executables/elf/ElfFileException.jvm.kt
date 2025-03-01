package space.iseki.executables.elf

import space.iseki.executables.common.CommonFileException

/**
 * JVM implementation of [ElfFileException].
 *
 * @param message the detailed message describing the error
 * @param cause the underlying reason for the exception
 */
actual open class ElfFileException @JvmOverloads actual constructor(
    message: String,
    override val cause: Throwable?,
) : CommonFileException(message, cause) 