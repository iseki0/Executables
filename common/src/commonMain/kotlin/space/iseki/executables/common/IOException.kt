@file:JvmName("IOException")

package space.iseki.executables.common

import kotlin.jvm.JvmName

/**
 * Represents an I/O exception.
 *
 * @param message the detail message, or null if no detail message is provided.
 * @param cause the cause of this exception, or null if no cause is specified.
 */
expect open class IOException(message: String?, cause: Throwable?) : Exception {
    constructor(message: String?)
    constructor(cause: Throwable?)
}

