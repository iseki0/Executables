@file:JvmName("-EOFException")

package space.iseki.executables.common

import kotlin.jvm.JvmName

expect class EOFException(message: String?) : IOException
