@file:JvmName("ByteArrayUtilsKt0")

package space.iseki.executables.share

import space.iseki.executables.common.CStringReadingException
import kotlin.jvm.JvmName

internal fun ByteArray.u2(offset: Int, littleEndian: Boolean): UShort = if (littleEndian) u2l(offset) else u2b(offset)
internal fun ByteArray.u4(offset: Int, littleEndian: Boolean): UInt = if (littleEndian) u4l(offset) else u4b(offset)
internal fun ByteArray.u8(offset: Int, littleEndian: Boolean): ULong = if (littleEndian) u8l(offset) else u8b(offset)

internal fun ByteArray.cstrUtf8(offset: Int): String {
    var end = offset
    while (end < size && this[end] != 0.toByte()) {
        end++
    }
    if (end >= size) {
        throw CStringReadingException(offset, CStringReadingException.Reason.NULL_TERMINATOR)
    }
    return decodeToString(offset, end)
}

internal expect fun ByteArray.wstr(offset: Int, length: Int): String

internal fun ByteArray.cwstr(offset: Int): String {
    var end = offset
    while (this.u2l(end) != 0u.toUShort()) {
        end += 2
    }
    return wstr(offset, end - offset)
}

