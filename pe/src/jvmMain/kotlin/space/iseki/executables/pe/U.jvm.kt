@file:JvmName("-U")

package space.iseki.executables.pe

internal actual fun <T> Array<T>.toUnmodifiableList(): List<T> = java.util.Collections.unmodifiableList(this.asList())
internal actual fun ByteArray.getWString(offset: Int, length: Int): String =
    java.lang.String(this, offset, length, Charsets.UTF_16LE) as String

internal actual fun <T> List<T>.toUnmodifiableList(): List<T> = java.util.Collections.unmodifiableList(this)
