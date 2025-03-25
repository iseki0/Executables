package space.iseki.executables.share

internal fun <T> List<T>.toUnmodifiableList(): List<T> = object : AbstractList<T>() {
    override val size: Int
        get() = this@toUnmodifiableList.size

    override fun get(index: Int): T = this@toUnmodifiableList[index]

}

internal fun <T> Array<T>.toUnmodifiableList(): List<T> = object : AbstractList<T>() {
    override val size: Int
        get() = this@toUnmodifiableList.size

    override fun get(index: Int): T = this@toUnmodifiableList[index]
}
