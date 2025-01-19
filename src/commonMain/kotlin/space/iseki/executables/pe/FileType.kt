package space.iseki.executables.pe

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class FileType(private val type: UInt) {
    object Constants {
        const val UNKNOWN = 0u
        const val APP = 1u
        const val DLL = 2u
        const val DRV = 3u
        const val FONT = 4u
        const val VXD = 5u
        const val STATIC_LIB = 7u
    }

    override fun toString(): String {
        return when (type) {
            Constants.UNKNOWN -> "UNKNOWN"
            Constants.APP -> "APP"
            Constants.DLL -> "DLL"
            Constants.DRV -> "DRV" 
            Constants.FONT -> "FONT"
            Constants.VXD -> "VXD"
            Constants.STATIC_LIB -> "STATIC_LIB"
            else -> "0x${type.toString(16)}"
        }
    }
} 