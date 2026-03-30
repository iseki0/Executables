package space.iseki.executables.pe.vi

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u4l

/**
 * Parsed `VS_FIXEDFILEINFO` from a PE version resource.
 *
 * This is the fixed, language-independent payload stored in `VS_VERSION_INFO`.
 * It matches the Win32 `VS_FIXEDFILEINFO` structure and is always 52 bytes long.
 *
 * The Windows version fields are split across two DWORD pairs:
 *
 * - `fileVersionMS`: major/minor
 * - `fileVersionLS`: build/revision
 * - `productVersionMS`: major/minor
 * - `productVersionLS`: build/revision
 *
 * See Microsoft Learn: `VS_FIXEDFILEINFO` and `VS_VERSIONINFO`.
 */
@Serializable
data class FixedFileInfo internal constructor(
    /**
     * Version of the fixed-file-info structure itself.
     *
     * The high word is the major structure version and the low word is the minor version.
     * In practice this is typically `0x00010000`, meaning structure version `1.0`.
     */
    val structVersion: UInt,

    /**
     * Most-significant 32 bits of the file version.
     *
     * High word = major, low word = minor.
     */
    val fileVersionMS: UInt,

    /**
     * Least-significant 32 bits of the file version.
     *
     * High word = build, low word = revision.
     */
    val fileVersionLS: UInt,

    /**
     * Most-significant 32 bits of the product version.
     *
     * High word = major, low word = minor.
     */
    val productVersionMS: UInt,

    /**
     * Least-significant 32 bits of the product version.
     *
     * High word = build, low word = revision.
     */
    val productVersionLS: UInt,

    /**
     * Bitmask describing which bits in [fileFlags] are meaningful.
     *
     * This is commonly `VS_FFI_FILEFLAGSMASK`.
     */
    val fileFlagsMask: UInt,

    /**
     * File attribute flags such as debug, prerelease, patched, private-build, or special-build.
     *
     * Only the bits enabled by [fileFlagsMask] are defined.
     */
    val fileFlags: FileInfoFlags,

    /**
     * Target operating system declared by the version resource.
     */
    val fileOS: FileOs,

    /**
     * General file type declared by the version resource.
     *
     * Typical values include application, DLL, driver, font, virtual device, and static library.
     */
    val fileType: FileType,

    /**
     * File subtype.
     *
     * This is mainly meaningful for some [fileType] values such as drivers and fonts.
     * For most other file types it is zero.
     */
    val fileSubtype: UInt,

    /**
     * Most-significant 32 bits of the file date.
     *
     * If the producer does not provide a date, both date fields are usually zero.
     */
    val fileDateMS: UInt,

    /**
     * Least-significant 32 bits of the file date.
     */
    val fileDateLS: UInt,
) : ReadableStructure {
    /**
     * File version decoded as `major.minor.build.revision`.
     */
    val fileVersion: Version
        get() = Version(
            (fileVersionMS shr 16).toUShort(),
            (fileVersionMS and 0xFFFFu).toUShort(),
            (fileVersionLS shr 16).toUShort(),
            (fileVersionLS and 0xFFFFu).toUShort(),
        )

    /**
     * Product version decoded as `major.minor.build.revision`.
     */
    val productVersion: Version
        get() = Version(
            (productVersionMS shr 16).toUShort(),
            (productVersionMS and 0xFFFFu).toUShort(),
            (productVersionLS shr 16).toUShort(),
            (productVersionLS and 0xFFFFu).toUShort(),
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

    companion object {
        /**
         * Size in bytes of `VS_FIXEDFILEINFO`.
         */
        const val LENGTH = 52

        /**
         * Required signature value for `VS_FIXEDFILEINFO`.
         */
        const val SIGNATURE = 0xFEEF04BDu

        /**
         * Parse `VS_FIXEDFILEINFO` from [bytes] at [offset].
         *
         * The first DWORD must be [SIGNATURE].
         */
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
                fileDateLS = bytes.u4l(offset + 48),
            )
        }
    }

    /**
     * Windows four-part version number represented as `major.minor.build.revision`.
     */
    @Serializable
    data class Version internal constructor(
        val major: UShort,
        val minor: UShort,
        val build: UShort,
        val revision: UShort,
    ) : Comparable<Version> {
        val ms: UInt
            get() = (major.toUInt() shl 16) or minor.toUInt()
        val ls: UInt
            get() = (build.toUInt() shl 16) or revision.toUInt()

        override fun compareTo(other: Version): Int =
            compareValuesBy(this, other, Version::major, Version::minor, Version::build, Version::revision)

        override fun toString(): String = "$major.$minor.$build.$revision"
    }
}
