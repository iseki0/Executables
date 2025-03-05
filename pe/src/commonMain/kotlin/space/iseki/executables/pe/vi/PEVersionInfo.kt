package space.iseki.executables.pe.vi

data class PEVersionInfo internal constructor(
    val fixedFileInfo: FixedFileInfo?,
    val stringFileInfo: StringTable?,
) {
}
