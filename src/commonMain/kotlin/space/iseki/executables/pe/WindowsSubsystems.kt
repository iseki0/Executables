package space.iseki.executables.pe

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
value class WindowsSubsystems(val rawValue: Short) {
    companion object {
        @JvmStatic
        fun valueOf(name: String): WindowsSubsystems = when (name) {
            "UNKNOWN" -> UNKNOWN
            "NATIVE" -> NATIVE
            "WINDOWS_GUI" -> WINDOWS_GUI
            "WINDOWS_CUI" -> WINDOWS_CUI
            "OS2_CUI" -> OS2_CUI
            "POSIX_CUI" -> POSIX_CUI
            "NATIVE_WINDOWS" -> NATIVE_WINDOWS
            "WINDOWS_CE_GUI" -> WINDOWS_CE_GUI
            "EFI_APPLICATION" -> EFI_APPLICATION
            "EFI_BOOT_SERVICE_DRIVER" -> EFI_BOOT_SERVICE_DRIVER
            "EFI_RUNTIME_DRIVER" -> EFI_RUNTIME_DRIVER
            "EFI_ROM" -> EFI_ROM
            "XBOX" -> XBOX
            "WINDOWS_BOOT_APPLICATION" -> WINDOWS_BOOT_APPLICATION
            else -> throw IllegalArgumentException("Unknown WindowsSubsystems: $name")
        }

        object Constants {
            const val UNKNOWN = 0.toShort()
            const val NATIVE = 1.toShort()
            const val WINDOWS_GUI = 2.toShort()
            const val WINDOWS_CUI = 3.toShort()
            const val OS2_CUI = 5.toShort()
            const val POSIX_CUI = 7.toShort()
            const val NATIVE_WINDOWS = 8.toShort()
            const val WINDOWS_CE_GUI = 9.toShort()
            const val EFI_APPLICATION = 10.toShort()
            const val EFI_BOOT_SERVICE_DRIVER = 11.toShort()
            const val EFI_RUNTIME_DRIVER = 12.toShort()
            const val EFI_ROM = 13.toShort()
            const val XBOX = 14.toShort()
            const val WINDOWS_BOOT_APPLICATION = 16.toShort()
        }

        val UNKNOWN = WindowsSubsystems(Constants.UNKNOWN)
        val NATIVE = WindowsSubsystems(Constants.NATIVE)
        val WINDOWS_GUI = WindowsSubsystems(Constants.WINDOWS_GUI)
        val WINDOWS_CUI = WindowsSubsystems(Constants.WINDOWS_CUI)
        val OS2_CUI = WindowsSubsystems(Constants.OS2_CUI)
        val POSIX_CUI = WindowsSubsystems(Constants.POSIX_CUI)
        val NATIVE_WINDOWS = WindowsSubsystems(Constants.NATIVE_WINDOWS)
        val WINDOWS_CE_GUI = WindowsSubsystems(Constants.WINDOWS_CE_GUI)
        val EFI_APPLICATION = WindowsSubsystems(Constants.EFI_APPLICATION)
        val EFI_BOOT_SERVICE_DRIVER = WindowsSubsystems(Constants.EFI_BOOT_SERVICE_DRIVER)
        val EFI_RUNTIME_DRIVER = WindowsSubsystems(Constants.EFI_RUNTIME_DRIVER)
        val EFI_ROM = WindowsSubsystems(Constants.EFI_ROM)
        val XBOX = WindowsSubsystems(Constants.XBOX)
        val WINDOWS_BOOT_APPLICATION = WindowsSubsystems(Constants.WINDOWS_BOOT_APPLICATION)
    }

    override fun toString(): String {
        return when (rawValue) {
            Constants.UNKNOWN -> "UNKNOWN"
            Constants.NATIVE -> "NATIVE"
            Constants.WINDOWS_GUI -> "WINDOWS_GUI"
            Constants.WINDOWS_CUI -> "WINDOWS_CUI"
            Constants.OS2_CUI -> "OS2_CUI"
            Constants.POSIX_CUI -> "POSIX_CUI"
            Constants.NATIVE_WINDOWS -> "NATIVE_WINDOWS"
            Constants.WINDOWS_CE_GUI -> "WINDOWS_CE_GUI"
            Constants.EFI_APPLICATION -> "EFI_APPLICATION"
            Constants.EFI_BOOT_SERVICE_DRIVER -> "EFI_BOOT_SERVICE_DRIVER"
            Constants.EFI_RUNTIME_DRIVER -> "EFI_RUNTIME_DRIVER"
            Constants.EFI_ROM -> "EFI_ROM"
            Constants.XBOX -> "XBOX"
            Constants.WINDOWS_BOOT_APPLICATION -> "WINDOWS_BOOT_APPLICATION"
            else -> "WindowsSubsystems(${rawValue.toUShort()})"
        }
    }
}
