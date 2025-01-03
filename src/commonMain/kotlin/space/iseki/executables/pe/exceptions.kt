package space.iseki.executables.pe

import kotlin.jvm.JvmOverloads

open class PEFileException @JvmOverloads constructor(message: String, override val cause: Throwable? = null) :
    RuntimeException(message, cause)

open class PEIOFileException @JvmOverloads constructor(message: String, override val cause: Throwable? = null) :
    PEFileException(message, cause)

class PEEOFFileException : PEIOFileException("Unexpected EOF reached")
