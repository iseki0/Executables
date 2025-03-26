package space.iseki.executables.common

actual class EOFException actual constructor(message: String?) : IOException(message)
actual open class IOException actual constructor(message: String?, cause: Throwable?) : Exception(message, cause) {
    actual constructor(message: String?) : this(message, null)

    actual constructor(cause: Throwable?) : this(null, cause)
}

actual class NoSuchFileException : IOException {
    actual constructor(file: String?, other: String?, reason: String?) : super(buildMessage(file, other, reason))
    actual constructor(file: String?) : super(file)
}

actual class AccessDeniedException : IOException {
    actual constructor(file: String?, other: String?, reason: String?) : super(buildMessage(file, other, reason))
    actual constructor(file: String?) : super(file)
}

private fun buildMessage(file: String?, other: String?, reason: String?) = buildString {
    if (file != null) {
        append(file)
    }
    if (other != null) {
        if (file != null) append(" -> ")
        append(other)
    }
    if (reason != null) {
        append(": ")
        append(reason)
    }
}
