package space.iseki.executables.pe

internal actual fun <T> Array<T>.toUnmodifiableList(): List<T> = this.toList()
