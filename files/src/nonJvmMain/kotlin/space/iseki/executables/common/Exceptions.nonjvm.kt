package space.iseki.executables.common

actual class EOFException actual constructor(message: String?) : IOException(message)
actual open class IOException actual constructor(message: String?, cause: Throwable?) : Exception(message, cause) {
    actual constructor(message: String?) : this(message, null)

    actual constructor(cause: Throwable?) : this(null, cause)
}
