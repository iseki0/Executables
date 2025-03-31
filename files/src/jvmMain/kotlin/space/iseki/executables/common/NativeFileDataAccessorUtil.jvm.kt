package space.iseki.executables.common

import java.nio.file.Path

internal actual fun openNativeFileDataAccessor(path: String): DataAccessor {
    return PathDataAccessor(Path.of(path))
}