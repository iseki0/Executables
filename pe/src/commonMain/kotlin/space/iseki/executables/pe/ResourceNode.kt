package space.iseki.executables.pe

interface ResourceNode {
    val name: String
    val id: UInt
    fun isFile(): Boolean
    fun isDirectory(): Boolean = !isFile()
    fun listChildren(): List<ResourceNode> = emptyList()
    val size: UInt
        get() = 0u
    val dataRva: Address32
    val codePage: CodePage
        get() = CodePage(0u)

    fun readAllBytes(): ByteArray = ByteArray(0)
}


