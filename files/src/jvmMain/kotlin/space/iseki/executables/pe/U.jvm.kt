@file:JvmName("-U")

package space.iseki.executables.pe

internal actual fun ByteArray.getWString(offset: Int, length: Int): String =
    java.lang.String(this, offset, length, Charsets.UTF_16LE) as String
