package space.iseki.executables.pe

internal actual fun ByteArray.getWString(offset: Int, length: Int): String {
    val chArray = CharArray(length / 2)
    for (i in 0 until length step 2) {
        chArray[i / 2] = (this[offset + i].toInt() and 0xff + (this[offset + i + 1].toInt() and 0xff shl 8)).toChar()
    }
    return chArray.concatToString()
}

internal actual fun <T> List<T>.toUnmodifiableList(): List<T> = object : List<T> by this {
    override fun subList(fromIndex: Int, toIndex: Int): List<T> = this.subList(fromIndex, toIndex).toUnmodifiableList()
}
