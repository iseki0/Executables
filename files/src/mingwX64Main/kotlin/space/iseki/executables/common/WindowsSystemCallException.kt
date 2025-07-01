package space.iseki.executables.common

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.value
import platform.windows.DWORD
import platform.windows.FORMAT_MESSAGE_ALLOCATE_BUFFER
import platform.windows.FORMAT_MESSAGE_FROM_SYSTEM
import platform.windows.FORMAT_MESSAGE_IGNORE_INSERTS
import platform.windows.FormatMessageW
import platform.windows.GetLastError
import platform.windows.LANG_NEUTRAL
import platform.windows.LPWSTRVar
import platform.windows.LocalFree
import platform.windows.SUBLANG_DEFAULT

/**
 * Exception representing a Windows system call failure.
 * This exception should be used when a Windows API call fails and the error is unrecoverable.
 * 
 * @param systemCall The name of the Windows system call that failed
 * @param errorCode The Windows error code returned by GetLastError(), or null to get it automatically
 * @param message Additional error message, or null to use the formatted Windows error message
 */
@OptIn(ExperimentalForeignApi::class)
class WindowsSystemCallException(
    val systemCall: String,
    val errorCode: DWORD = GetLastError(),
    message: String? = null
) : RuntimeException(message ?: formatWindowsError(systemCall, errorCode)) {
    
    companion object {
        private fun makeLangId(primary: Int, sub: Int) = (sub shl 10) or primary
        
        private fun formatWindowsError(systemCall: String, errorCode: DWORD): String {
            val osMessage = formatMessage(errorCode)
            return buildString {
                append("Windows system call failed: ")
                append(systemCall)
                append(" (error code: ")
                append(errorCode)
                append(")")
                if (osMessage.isNotEmpty()) {
                    append(" - ")
                    append(osMessage)
                }
            }
        }
        
        private fun formatMessage(errorCode: DWORD): String {
            memScoped {
                val buffer = alloc<LPWSTRVar>()
                val size = FormatMessageW(
                    (FORMAT_MESSAGE_ALLOCATE_BUFFER or FORMAT_MESSAGE_FROM_SYSTEM or FORMAT_MESSAGE_IGNORE_INSERTS).toUInt(),
                    null,
                    errorCode,
                    makeLangId(LANG_NEUTRAL, SUBLANG_DEFAULT).toUInt(),
                    buffer.ptr.reinterpret(),
                    0u,
                    null,
                )

                if (size == 0u) return ""
                val message = buffer.value?.toKStringFromUtf16().orEmpty()
                LocalFree(buffer.value)
                return message.trim()
            }
        }
    }
}

/**
 * Terminates the program with an unhandled exception.
 * This should be called when a critical system call fails and the program cannot continue safely.
 * 
 * @param exception The exception that caused the termination
 */
fun terminateWithUnhandledException(exception: Throwable): Nothing {
    // Print the exception to stderr
    println("FATAL: Unhandled exception - terminating program")
    println("Exception: ${exception::class.simpleName}: ${exception.message}")
    exception.printStackTrace()
    
    // Terminate the process with a non-zero exit code
    kotlin.system.exitProcess(1)
}