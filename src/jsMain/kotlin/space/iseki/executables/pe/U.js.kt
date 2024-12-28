package space.iseki.executables.pe

internal actual fun ByteArray.getUShort(offset: Int): UShort {
    var r = 0
    repeat(2) { i ->
        r = r or (this[offset + i].toInt() shl (i * 8))
    }
    return r.toUShort()
}

internal actual fun ByteArray.getUInt(offset: Int): UInt {
    var r = 0
    repeat(4) { i ->
        r = r or (this[offset + i].toInt() shl (i * 8))
    }
    return r.toUInt()
}

internal actual fun ByteArray.getULong(offset: Int): ULong {
    var r = 0L
    repeat(8) { i ->
        r = r or (this[offset + i].toLong() shl (i * 8))
    }
    return r.toULong()
}
