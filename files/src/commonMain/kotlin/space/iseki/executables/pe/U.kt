package space.iseki.executables.pe

import space.iseki.executables.share.u2l

internal expect fun ByteArray.getWString(offset: Int, length: Int): String

/**
 * Read a null-terminated wide string from a byte array
 */
internal fun ByteArray.getWString(offset: Int): String {
    var end = offset
    while (this.u2l(end) != 0u.toUShort()) {
        end += 2
    }
    return getWString(offset, end - offset)
}
