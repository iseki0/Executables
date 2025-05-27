package space.iseki.executablestool

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.value
import platform.windows.CommandLineToArgvW
import platform.windows.GetCommandLineW
import platform.windows.GetLastError
import platform.windows.LocalFree

@OptIn(ExperimentalForeignApi::class)
internal actual fun cmdlineArgs(): List<String>? {
    memScoped {
        val n = alloc<IntVar>()
        val p = CommandLineToArgvW(GetCommandLineW()?.toKStringFromUtf16().orEmpty(), n.ptr)
            ?: error("Error while calling command line: ${GetLastError()}")
        return buildList(n.value - 1) {
            for (i in 1 until n.value) {
                add(p[i]?.toKStringFromUtf16().orEmpty())
            }
        }.also { LocalFree(p) }
    }
}