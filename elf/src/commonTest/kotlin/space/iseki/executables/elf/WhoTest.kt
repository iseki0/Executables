package space.iseki.executables.elf

import kotlinx.serialization.json.Json
import space.iseki.executables.common.ExecutableFileType
import kotlin.test.Test
import kotlin.test.assertEquals

class WhoTest {
    private val json = Json {
        prettyPrint = true
    }

    @Test
    fun test() {
        assertEquals(ExecutableFileType.ELF, ExecutableFileType.detect(whoData))
        val file = ElfFile(whoData)
        val ident = json.encodeToString(file.ident)
        val ehdr = json.encodeToString(file.ehdr)
        println(ident)
        println(ehdr)
        """
            {
                "eiClass": "ELFCLASS64",
                "eiData": "ELFDATA2LSB",
                "eiVersion": 1,
                "eiOsAbi": "ELFOSABI_NONE",
                "eiAbiVersion": 0
            }
        """.trimIndent().let { Json.decodeFromString<ElfIdentification>(it) }.also { assertEquals(it, file.ident) }
        """
            {
                "type": "space.iseki.executables.elf.Elf64Ehdr",
                "eType": "ET_DYN",
                "eMachine": "X86_64",
                "eVersion": 1,
                "eEntry": "0x0000000000002c80",
                "ePhoff": 64,
                "eShoff": 49744,
                "eFlags": 0,
                "eEhsize": 64,
                "ePhentsize": 56,
                "ePhnum": 13,
                "eShentsize": 64,
                "eShnum": 31,
                "eShstrndx": 30
            }
        """.trimIndent().let { Json.decodeFromString<ElfEhdr>(it) }.also { assertEquals(it, file.ehdr) }
        println(ident)
        println(ehdr)
    }
}