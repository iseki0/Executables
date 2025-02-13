package space.iseki.executables.pe

internal expect fun ByteArray.getUShort(offset: Int): UShort
internal expect fun ByteArray.getUInt(offset: Int): UInt
internal expect fun ByteArray.getULong(offset: Int): ULong
internal expect fun <T> Array<T>.toUnmodifiableList(): List<T>
internal expect fun <T> List<T>.toUnmodifiableList(): List<T>
internal expect fun ByteArray.getWString(offset: Int, length: Int): String

/**
 * Read a null-terminated wide string from a byte array
 */
internal fun ByteArray.getWString(offset: Int): String {
    var end = offset
    while (this.getUShort(end) != 0u.toUShort()) {
        end += 2
    }
    return getWString(offset, end - offset)
}
