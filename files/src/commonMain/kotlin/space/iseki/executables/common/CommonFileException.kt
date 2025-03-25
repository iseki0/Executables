package space.iseki.executables.common

import kotlin.jvm.JvmOverloads

open class CommonFileException @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)
