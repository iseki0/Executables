package space.iseki.executables.common

internal actual fun openNativeFileDataAccessor(path: String): DataAccessor {
    return NativeFileDataAccessor(path)
}