package space.iseki.executables.pe

internal actual fun ByteArray.getUShort(offset: Int): UShort {
    var r = 0
    for (i in 1 downTo 0) {
        r = r shl 8
        r = r or (this[offset + i].toInt() and 0xff)
    }
    return r.toUShort()
}

internal actual fun ByteArray.getUInt(offset: Int): UInt {
    var r = 0
    for (i in 3 downTo 0) {
        r = r shl 8
        r = r or (this[offset + i].toInt() and 0xff)
    }
    return r.toUInt()
}

internal actual fun ByteArray.getULong(offset: Int): ULong {
    var r = 0L
    for (i in 7 downTo 0) {
        r = r shl 8
        r = r or (this[offset + i].toLong() and 0xff)
    }
    return r.toULong()
}

internal actual fun <T> Array<T>.toUnmodifiableList(): List<T> = this.toList()
