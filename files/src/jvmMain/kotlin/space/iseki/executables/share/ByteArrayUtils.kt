package space.iseki.executables.share


internal actual fun ByteArray.wstr(offset: Int, length: Int): String {
    return java.lang.String(this, offset, length, Charsets.UTF_16LE) as String
}

