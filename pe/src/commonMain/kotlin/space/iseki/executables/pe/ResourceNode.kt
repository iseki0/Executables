package space.iseki.executables.pe

import kotlin.jvm.JvmName

/**
 * Represents a resource node in a PE file.
 *
 * @property name the name of the resource node
 * @property id the identifier of the resource node
 * @property dataRva the relative virtual address of the resource data
 * @property codePage the code page associated with this resource
 * @property size the size of the resource; returns 0 if not applicable
 */

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

    fun getPEFile(): PEFile
    fun readAllBytes(): ByteArray = ByteArray(0)
}


