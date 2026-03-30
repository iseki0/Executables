package space.iseki.executables.pe.vi

/**
 * Parsed high-level view of a PE `VS_VERSION_INFO` resource.
 *
 * This combines:
 *
 * - the optional fixed binary block [fixedFileInfo] (`VS_FIXEDFILEINFO`)
 * - the optional localized string table [stringFileInfo] from `StringFileInfo`
 *
 * The underlying Win32 format may also contain `VarFileInfo`, but the current parser only
 * exposes the fixed block and one parsed string table.
 *
 * See Microsoft Learn: `VS_VERSIONINFO` and Version Information.
 */
data class PEVersionInfo internal constructor(
    /**
     * Language-independent fixed metadata from `VS_FIXEDFILEINFO`.
     *
     * This is `null` when `VS_VERSION_INFO.wValueLength == 0`.
     */
    val fixedFileInfo: FixedFileInfo?,

    /**
     * Localized string metadata extracted from `StringFileInfo`.
     *
     * This is `null` when the version resource contains no `StringFileInfo` block.
     */
    val stringFileInfo: StringTable?,
)
