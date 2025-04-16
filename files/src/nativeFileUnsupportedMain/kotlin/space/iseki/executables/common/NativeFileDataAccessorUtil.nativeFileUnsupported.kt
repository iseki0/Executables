package space.iseki.executables.common

internal actual fun openNativeFileDataAccessor(path: String): DataAccessor {
    throw UnsupportedOperationException("Native file access is not supported on this platform.")
}