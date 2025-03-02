@file:JvmName("-U")

package space.iseki.executables.pe

internal fun <T> Array<T>.toUnmodifiableList(): List<T> = java.util.Collections.unmodifiableList(this.asList())
internal fun ByteArray.getWString(offset: Int, length: Int): String =
    java.lang.String(this, offset, length, Charsets.UTF_16LE) as String

internal fun <T> List<T>.toUnmodifiableList(): List<T> = java.util.Collections.unmodifiableList(this)
