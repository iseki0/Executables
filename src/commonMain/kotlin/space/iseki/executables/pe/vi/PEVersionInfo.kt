package space.iseki.executables.pe.vi

class PEVersionInfo internal constructor(
    val fixedFileInfo: FixedFileInfo?,
    val stringFileInfo: StringTable?,
) {
    override fun toString(): String {
        return "PEVersionInfo(fixedFileInfo=$fixedFileInfo, stringFileInfo=$stringFileInfo)"
    }
}
