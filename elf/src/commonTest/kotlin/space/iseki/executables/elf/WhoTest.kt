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
        val phdrs = json.encodeToString(file.programHeaders)
        val shdrs = json.encodeToString(file.sectionHeaders)
        println(ident)
        println(ehdr)
        println(phdrs)
        println(shdrs)
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
                "type": "Elf64Ehdr",
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

        """
            [
                {
                    "type": "Elf64Phdr",
                    "pType": "PT_PHDR",
                    "pFlags": [
                        "PF_R"
                    ],
                    "pOffset": 64,
                    "pVaddr": "0x0000000000000040",
                    "pPaddr": "0x0000000000000040",
                    "pFilesz": 728,
                    "pMemsz": 728,
                    "pAlign": 8
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "PT_INTERP",
                    "pFlags": [
                        "PF_R"
                    ],
                    "pOffset": 792,
                    "pVaddr": "0x0000000000000318",
                    "pPaddr": "0x0000000000000318",
                    "pFilesz": 28,
                    "pMemsz": 28,
                    "pAlign": 1
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "PT_LOAD",
                    "pFlags": [
                        "PF_R"
                    ],
                    "pOffset": 0,
                    "pVaddr": "0x0000000000000000",
                    "pPaddr": "0x0000000000000000",
                    "pFilesz": 6232,
                    "pMemsz": 6232,
                    "pAlign": 4096
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "PT_LOAD",
                    "pFlags": [
                        "PF_X",
                        "PF_R"
                    ],
                    "pOffset": 8192,
                    "pVaddr": "0x0000000000002000",
                    "pPaddr": "0x0000000000002000",
                    "pFilesz": 27014,
                    "pMemsz": 27014,
                    "pAlign": 4096
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "PT_LOAD",
                    "pFlags": [
                        "PF_R"
                    ],
                    "pOffset": 36864,
                    "pVaddr": "0x0000000000009000",
                    "pPaddr": "0x0000000000009000",
                    "pFilesz": 7056,
                    "pMemsz": 7056,
                    "pAlign": 4096
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "PT_LOAD",
                    "pFlags": [
                        "PF_W",
                        "PF_R"
                    ],
                    "pOffset": 47456,
                    "pVaddr": "0x000000000000c960",
                    "pPaddr": "0x000000000000c960",
                    "pFilesz": 1864,
                    "pMemsz": 2392,
                    "pAlign": 4096
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "PT_DYNAMIC",
                    "pFlags": [
                        "PF_W",
                        "PF_R"
                    ],
                    "pOffset": 48096,
                    "pVaddr": "0x000000000000cbe0",
                    "pPaddr": "0x000000000000cbe0",
                    "pFilesz": 432,
                    "pMemsz": 432,
                    "pAlign": 8
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "PT_NOTE",
                    "pFlags": [
                        "PF_R"
                    ],
                    "pOffset": 824,
                    "pVaddr": "0x0000000000000338",
                    "pPaddr": "0x0000000000000338",
                    "pFilesz": 48,
                    "pMemsz": 48,
                    "pAlign": 8
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "PT_NOTE",
                    "pFlags": [
                        "PF_R"
                    ],
                    "pOffset": 872,
                    "pVaddr": "0x0000000000000368",
                    "pPaddr": "0x0000000000000368",
                    "pFilesz": 68,
                    "pMemsz": 68,
                    "pAlign": 4
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "0x6474e553",
                    "pFlags": [
                        "PF_R"
                    ],
                    "pOffset": 824,
                    "pVaddr": "0x0000000000000338",
                    "pPaddr": "0x0000000000000338",
                    "pFilesz": 48,
                    "pMemsz": 48,
                    "pAlign": 8
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "0x6474e550",
                    "pFlags": [
                        "PF_R"
                    ],
                    "pOffset": 42108,
                    "pVaddr": "0x000000000000a47c",
                    "pPaddr": "0x000000000000a47c",
                    "pFilesz": 212,
                    "pMemsz": 212,
                    "pAlign": 4
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "PT_GNU_STACK",
                    "pFlags": [
                        "PF_W",
                        "PF_R"
                    ],
                    "pOffset": 0,
                    "pVaddr": "0x0000000000000000",
                    "pPaddr": "0x0000000000000000",
                    "pFilesz": 0,
                    "pMemsz": 0,
                    "pAlign": 16
                },
                {
                    "type": "Elf64Phdr",
                    "pType": "0x6474e552",
                    "pFlags": [
                        "PF_R"
                    ],
                    "pOffset": 47456,
                    "pVaddr": "0x000000000000c960",
                    "pPaddr": "0x000000000000c960",
                    "pFilesz": 1696,
                    "pMemsz": 1696,
                    "pAlign": 1
                }
            ]

        """.trimIndent().let { Json.decodeFromString<List<ElfPhdr>>(it) }.also { assertEquals(it, file.programHeaders) }


        """
            [
                {
                    "type": "Elf64Shdr",
                    "shName": 0,
                    "shType": "SHT_NULL",
                    "shFlags": [],
                    "shAddr": "0x0000000000000000",
                    "shOffset": 0,
                    "shSize": 0,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 0,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 11,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x0000000000000318",
                    "shOffset": 792,
                    "shSize": 28,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 1,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 19,
                    "shType": "SHT_NOTE",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x0000000000000338",
                    "shOffset": 824,
                    "shSize": 48,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 8,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 38,
                    "shType": "SHT_NOTE",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x0000000000000368",
                    "shOffset": 872,
                    "shSize": 36,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 4,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 57,
                    "shType": "SHT_NOTE",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x000000000000038c",
                    "shOffset": 908,
                    "shSize": 32,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 4,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 71,
                    "shType": "0x6ffffff6",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x00000000000003b0",
                    "shOffset": 944,
                    "shSize": 36,
                    "shLink": 6,
                    "shInfo": 0,
                    "shAddralign": 8,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 81,
                    "shType": "SHT_DYNSYM",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x00000000000003d8",
                    "shOffset": 984,
                    "shSize": 1824,
                    "shLink": 7,
                    "shInfo": 1,
                    "shAddralign": 8,
                    "shEntsize": 24
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 89,
                    "shType": "SHT_STRTAB",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x0000000000000af8",
                    "shOffset": 2808,
                    "shSize": 881,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 1,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 97,
                    "shType": "0x6fffffff",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x0000000000000e6a",
                    "shOffset": 3690,
                    "shSize": 152,
                    "shLink": 6,
                    "shInfo": 0,
                    "shAddralign": 2,
                    "shEntsize": 2
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 110,
                    "shType": "0x6ffffffe",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x0000000000000f08",
                    "shOffset": 3848,
                    "shSize": 128,
                    "shLink": 7,
                    "shInfo": 1,
                    "shAddralign": 8,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 125,
                    "shType": "SHT_RELA",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x0000000000000f88",
                    "shOffset": 3976,
                    "shSize": 744,
                    "shLink": 6,
                    "shInfo": 0,
                    "shAddralign": 8,
                    "shEntsize": 24
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 135,
                    "shType": "SHT_RELA",
                    "shFlags": [
                        "SHF_ALLOC",
                        "0x0000000000000040"
                    ],
                    "shAddr": "0x0000000000001270",
                    "shOffset": 4720,
                    "shSize": 1512,
                    "shLink": 6,
                    "shInfo": 25,
                    "shAddralign": 8,
                    "shEntsize": 24
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 145,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_ALLOC",
                        "SHF_EXECINSTR"
                    ],
                    "shAddr": "0x0000000000002000",
                    "shOffset": 8192,
                    "shSize": 37,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 4,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 140,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_ALLOC",
                        "SHF_EXECINSTR"
                    ],
                    "shAddr": "0x0000000000002030",
                    "shOffset": 8240,
                    "shSize": 1024,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 16,
                    "shEntsize": 16
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 151,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_ALLOC",
                        "SHF_EXECINSTR"
                    ],
                    "shAddr": "0x0000000000002430",
                    "shOffset": 9264,
                    "shSize": 16,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 16,
                    "shEntsize": 16
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 160,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_ALLOC",
                        "SHF_EXECINSTR"
                    ],
                    "shAddr": "0x0000000000002440",
                    "shOffset": 9280,
                    "shSize": 1008,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 16,
                    "shEntsize": 16
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 169,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_ALLOC",
                        "SHF_EXECINSTR"
                    ],
                    "shAddr": "0x0000000000002830",
                    "shOffset": 10288,
                    "shSize": 24897,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 16,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 175,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_ALLOC",
                        "SHF_EXECINSTR"
                    ],
                    "shAddr": "0x0000000000008974",
                    "shOffset": 35188,
                    "shSize": 18,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 4,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 181,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x0000000000009000",
                    "shOffset": 36864,
                    "shSize": 5244,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 32,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 189,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x000000000000a47c",
                    "shOffset": 42108,
                    "shSize": 212,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 4,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 203,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x000000000000a550",
                    "shOffset": 42320,
                    "shSize": 1600,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 8,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 213,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_WRITE",
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x000000000000c960",
                    "shOffset": 47456,
                    "shSize": 16,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 8,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 220,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_WRITE",
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x000000000000c970",
                    "shOffset": 47472,
                    "shSize": 16,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 8,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 227,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_WRITE",
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x000000000000c980",
                    "shOffset": 47488,
                    "shSize": 608,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 32,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 240,
                    "shType": "SHT_DYNAMIC",
                    "shFlags": [
                        "SHF_WRITE",
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x000000000000cbe0",
                    "shOffset": 48096,
                    "shSize": 432,
                    "shLink": 7,
                    "shInfo": 0,
                    "shAddralign": 8,
                    "shEntsize": 16
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 155,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_WRITE",
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x000000000000cd90",
                    "shOffset": 48528,
                    "shSize": 608,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 8,
                    "shEntsize": 8
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 249,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [
                        "SHF_WRITE",
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x000000000000d000",
                    "shOffset": 49152,
                    "shSize": 168,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 32,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 255,
                    "shType": "SHT_NOBITS",
                    "shFlags": [
                        "SHF_WRITE",
                        "SHF_ALLOC"
                    ],
                    "shAddr": "0x000000000000d0c0",
                    "shOffset": 49320,
                    "shSize": 504,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 32,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 260,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [],
                    "shAddr": "0x0000000000000000",
                    "shOffset": 49320,
                    "shSize": 73,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 1,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 278,
                    "shType": "SHT_PROGBITS",
                    "shFlags": [],
                    "shAddr": "0x0000000000000000",
                    "shOffset": 49396,
                    "shSize": 52,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 4,
                    "shEntsize": 0
                },
                {
                    "type": "Elf64Shdr",
                    "shName": 1,
                    "shType": "SHT_STRTAB",
                    "shFlags": [],
                    "shAddr": "0x0000000000000000",
                    "shOffset": 49448,
                    "shSize": 293,
                    "shLink": 0,
                    "shInfo": 0,
                    "shAddralign": 1,
                    "shEntsize": 0
                }
            ]
        """.trimIndent().let { Json.decodeFromString<List<ElfShdr>>(it) }.also { assertEquals(it, file.sectionHeaders) }
    }
}