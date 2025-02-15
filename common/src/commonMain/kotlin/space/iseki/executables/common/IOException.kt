@file:JvmName("IOException")

package space.iseki.executables.common

import kotlin.jvm.JvmName

expect open class IOException(message: String?, cause: Throwable?) : Exception {
    constructor(message: String?)
    constructor(cause: Throwable?)
}

