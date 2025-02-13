package space.iseki.executables.pe

import kotlin.jvm.JvmOverloads

open class PEFileException @JvmOverloads constructor(message: String, override val cause: Throwable? = null) :
    RuntimeException(message, cause)
