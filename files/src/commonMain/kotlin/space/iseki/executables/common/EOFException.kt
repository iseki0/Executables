@file:JvmName("-EOFException")

package space.iseki.executables.common

import kotlin.jvm.JvmName

/**
 * Represents an end-of-file exception.
 *
 * @param message the detail message, or null if no detail message is provided.
 */
expect class EOFException(message: String?) : IOException
