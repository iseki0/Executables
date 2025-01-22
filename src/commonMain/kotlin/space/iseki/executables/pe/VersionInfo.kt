package space.iseki.executables.pe

import kotlinx.serialization.Serializable

data class VersionInfo(
    val signature: UShort,  // 必须是 0xFEEF
    val structVersion: UShort, // 通常是 0x0100
    val fileVersionMS: UInt,
    val fileVersionLS: UInt,
    val productVersionMS: UInt,
    val productVersionLS: UInt,
    val fileFlagsMask: UInt,
    val fileFlags: FileInfoFlags,
    val fileOS: FileOs,
    val fileType: FileType,
    val fileSubtype: UInt,
    val fileDateMS: UInt,
    val fileDateLS: UInt,
) {
    val fileVersion: Version
        get() = Version(
            (fileVersionMS shr 16).toUShort(),  // Major (high word of MS)
            (fileVersionMS and 0xFFFFu).toUShort(),  // Minor (low word of MS)
            (fileVersionLS shr 16).toUShort(),  // Build (high word of LS)
            (fileVersionLS and 0xFFFFu).toUShort()   // Patch (low word of LS)
        )

    val productVersion: Version
        get() = Version(
            (productVersionMS shr 16).toUShort(),  // Major
            (productVersionMS and 0xFFFFu).toUShort(),  // Minor
            (productVersionLS shr 16).toUShort(),  // Build
            (productVersionLS and 0xFFFFu).toUShort()   // Patch
        )

    override fun toString(): String {
        return """
            |VersionInfo(
            |   signature = $signature,
            |   structVersion = $structVersion,
            |   fileVersion = $fileVersion,
            |   productVersion = $productVersion,
            |   fileFlagsMask = $fileFlagsMask,
            |   fileFlags = $fileFlags,
            |   fileOS = $fileOS,
            |   fileType = $fileType,
            |   fileSubtype = $fileSubtype,
            |   fileDateMS = $fileDateMS,
            |   fileDateLS = $fileDateLS
            |)
        """.trimMargin()
    }

    companion object {
        const val LENGTH = 52
        const val SIGNATURE = 0xFEEF.toShort()

        fun parse(bytes: ByteArray, offset: Int): VersionInfo {
            val signature = bytes.getUShort(offset)
            require(signature == SIGNATURE.toUShort()) { "Invalid VS_VERSIONINFO signature: $signature" }

            return VersionInfo(
                signature = signature,
                structVersion = bytes.getUShort(offset + 2),
                fileVersionMS = bytes.getUInt(offset + 4),
                fileVersionLS = bytes.getUInt(offset + 8),
                productVersionMS = bytes.getUInt(offset + 12),
                productVersionLS = bytes.getUInt(offset + 16),
                fileFlagsMask = bytes.getUInt(offset + 20),
                fileFlags = FileInfoFlags(bytes.getUInt(offset + 24)),
                fileOS = FileOs(bytes.getUInt(offset + 28)),
                fileType = FileType(bytes.getUInt(offset + 32)),
                fileSubtype = bytes.getUInt(offset + 36),
                fileDateMS = bytes.getUInt(offset + 40),
                fileDateLS = bytes.getUInt(offset + 44)
            )
        }
    }

    @Serializable
    data class Version(
        val major: UShort,
        val minor: UShort,
        val build: UShort,
        val revision: UShort,
    ) {
        override fun toString(): String = "$major.$minor.$build.$revision"
    }
}
