package space.iseki.executables.pe.vi

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u4l

@Serializable
data class FixedFileInfo(
    val structVersion: UInt, // 通常是 0x00000100
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
) : ReadableStructure {
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

    override val fields: Map<String, Any>
        get() = mapOf(
            "structVersion" to structVersion,
            "fileVersion" to fileVersion,
            "productVersion" to productVersion,
            "fileFlagsMask" to fileFlagsMask,
            "fileFlags" to fileFlags,
            "fileOS" to fileOS,
            "fileType" to fileType,
            "fileSubtype" to fileSubtype,
            "fileDateMS" to fileDateMS,
            "fileDateLS" to fileDateLS,
        )

    override fun toString(): String = fields.entries.joinToString("", "VersionInfo(", ")") { (k, v) -> "   $k = $v,\n" }

    companion object {
        const val LENGTH = 52
        const val SIGNATURE = 0xFEEF04BDu

        fun parse(bytes: ByteArray, offset: Int): FixedFileInfo {
            val signature = bytes.u4l(offset)
            require(signature == SIGNATURE) { "Invalid VS_VERSIONINFO signature: $signature" }

            return FixedFileInfo(
                structVersion = bytes.u4l(offset + 4),
                fileVersionMS = bytes.u4l(offset + 8),
                fileVersionLS = bytes.u4l(offset + 12),
                productVersionMS = bytes.u4l(offset + 16),
                productVersionLS = bytes.u4l(offset + 20),
                fileFlagsMask = bytes.u4l(offset + 24),
                fileFlags = FileInfoFlags(bytes.u4l(offset + 28)),
                fileOS = FileOs(bytes.u4l(offset + 32)),
                fileType = FileType(bytes.u4l(offset + 36)),
                fileSubtype = bytes.u4l(offset + 40),
                fileDateMS = bytes.u4l(offset + 44),
                fileDateLS = bytes.u4l(offset + 48)
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
        val ms: UInt
            get() = (major.toUInt() shl 16) or minor.toUInt()
        val ls: UInt
            get() = (build.toUInt() shl 16) or revision.toUInt()

        override fun toString(): String = "$major.$minor.$build.$revision"
    }
}
