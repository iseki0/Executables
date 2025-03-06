package space.iseki.executables.pe.vi

@ConsistentCopyVisibility
data class PEVersionInfo internal constructor(
    val fixedFileInfo: FixedFileInfo?,
    val stringFileInfo: StringTable?,
)
