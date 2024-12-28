package space.iseki.executables.pe

import kotlin.jvm.JvmOverloads

open class PEException @JvmOverloads constructor(message: String, override val cause: Throwable? = null) :
    RuntimeException(message, cause)

open class PEIOException @JvmOverloads constructor(message: String, override val cause: Throwable? = null) :
    PEException(message, cause)

class PEEOFException : PEIOException("Unexpected EOF reached")
