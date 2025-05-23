package space.iseki.executables.elf

import space.iseki.executables.common.toAddr
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * 测试ELF相关类的parse函数
 */
class ElfParseTest {

    @Test
    fun testElfIdentificationParse() {
        // 创建一个有效的ELF标识字节数组
        val validBytes = byteArrayOf(
            0x7F, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(), // 魔数
            2.toByte(), // ELFCLASS64
            1.toByte(), // ELFDATA2LSB
            1, // 版本
            3.toByte(), // ELFOSABI_LINUX
            0, // ABI版本
            0, 0, 0, 0, 0, 0, 0 // 填充
        )

        // 测试解析
        val ident = ElfIdentification.parse(validBytes, 0)

        // 验证解析结果
        assertEquals(ElfClass.ELFCLASS64, ident.eiClass)
        assertEquals(ElfData.ELFDATA2LSB, ident.eiData)
        assertEquals(1u.toUByte(), ident.eiVersion)
        assertEquals(ElfOsAbi.ELFOSABI_LINUX, ident.eiOsAbi)
        assertEquals(0u.toUByte(), ident.eiAbiVersion)

        // 测试无效的魔数
        val invalidMagicBytes = byteArrayOf(
            0x7E, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(), // 错误的魔数
            2.toByte(), // ELFCLASS64
            1.toByte(), // ELFDATA2LSB
            1,
            3.toByte(), // ELFOSABI_LINUX
            0,
            0, 0, 0, 0, 0, 0, 0
        )

        assertFailsWith<ElfFileException> {
            ElfIdentification.parse(invalidMagicBytes, 0)
        }

        // 测试数据不足
        val tooShortBytes = byteArrayOf(0x7F, 'E'.code.toByte(), 'L'.code.toByte())

        assertFailsWith<ElfFileException> {
            ElfIdentification.parse(tooShortBytes, 0)
        }
    }

    @Test
    fun testElf32EhdrParse() {
        // 创建一个有效的ELF标识
        val identBytes = byteArrayOf(
            0x7F, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(),
            1.toByte(), // ELFCLASS32
            1.toByte(), // ELFDATA2LSB 小端序
            1,
            3.toByte(), // ELFOSABI_LINUX
            0,
            0, 0, 0, 0, 0, 0, 0
        )
        val ident = ElfIdentification.parse(identBytes, 0)

        // 创建一个有效的ELF32头部字节数组（小端序）
        val headerBytes = ByteArray(52) // ELF32头部大小为52字节

        // 填充ELF标识
        identBytes.copyInto(headerBytes, 0, 0, 16)

        // 填充头部其余部分（小端序）
        // e_type: ET_EXEC (2)
        headerBytes[16] = 2
        headerBytes[17] = 0

        // e_machine: EM_386 (3)
        headerBytes[18] = 3
        headerBytes[19] = 0

        // e_version: EV_CURRENT (1)
        headerBytes[20] = 1
        headerBytes[21] = 0
        headerBytes[22] = 0
        headerBytes[23] = 0

        // e_entry: 0x8048000
        headerBytes[24] = 0x00.toByte()
        headerBytes[25] = 0x80.toByte()
        headerBytes[26] = 0x04.toByte()
        headerBytes[27] = 0x08.toByte()

        // e_phoff: 52
        headerBytes[28] = 52
        headerBytes[29] = 0
        headerBytes[30] = 0
        headerBytes[31] = 0

        // e_shoff: 2104
        headerBytes[32] = 0x38.toByte()
        headerBytes[33] = 0x08.toByte()
        headerBytes[34] = 0
        headerBytes[35] = 0

        // e_flags: 0
        headerBytes[36] = 0
        headerBytes[37] = 0
        headerBytes[38] = 0
        headerBytes[39] = 0

        // e_ehsize: 52
        headerBytes[40] = 52
        headerBytes[41] = 0

        // e_phentsize: 32
        headerBytes[42] = 32
        headerBytes[43] = 0

        // e_phnum: 8
        headerBytes[44] = 8
        headerBytes[45] = 0

        // e_shentsize: 40
        headerBytes[46] = 40
        headerBytes[47] = 0

        // e_shnum: 25
        headerBytes[48] = 25
        headerBytes[49] = 0

        // e_shstrndx: 24
        headerBytes[50] = 24
        headerBytes[51] = 0

        // 测试解析
        val ehdr = ElfEhdr.parse32(headerBytes, 0, ident)

        // 验证解析结果
        assertEquals(false, ehdr.is64Bit)
        assertEquals(ElfType.ET_EXEC, ehdr.eType)
        assertEquals(ElfMachine.I386, ehdr.eMachine)
        assertEquals(1u, ehdr.eVersion)
        assertEquals(0x8048000UL.toAddr(), ehdr.eEntry)
        assertEquals(52UL, ehdr.ePhoff)
        assertEquals(2104UL, ehdr.eShoff)
        assertEquals(0u, ehdr.eFlags)
        assertEquals(52u.toUShort(), ehdr.eEhsize)
        assertEquals(32u.toUShort(), ehdr.ePhentsize)
        assertEquals(8u.toUShort(), ehdr.ePhnum)
        assertEquals(40u.toUShort(), ehdr.eShentsize)
        assertEquals(25u.toUShort(), ehdr.eShnum)
        assertEquals(24u.toUShort(), ehdr.eShstrndx)
    }

    @Test
    fun testElf64EhdrParse() {
        // 创建一个有效的ELF标识
        val identBytes = byteArrayOf(
            0x7F, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(),
            2.toByte(), // ELFCLASS64
            2.toByte(), // ELFDATA2MSB 大端序
            1,
            9.toByte(), // ELFOSABI_FREEBSD
            0,
            0, 0, 0, 0, 0, 0, 0
        )
        val ident = ElfIdentification.parse(identBytes, 0)

        // 创建一个有效的ELF64头部字节数组（大端序）
        val headerBytes = ByteArray(64) // ELF64头部大小为64字节

        // 填充ELF标识
        identBytes.copyInto(headerBytes, 0, 0, 16)

        // 填充头部其余部分（大端序）
        // e_type: ET_DYN (3)
        headerBytes[16] = 0
        headerBytes[17] = 3

        // e_machine: EM_X86_64 (62)
        headerBytes[18] = 0
        headerBytes[19] = 62

        // e_version: EV_CURRENT (1)
        headerBytes[20] = 0
        headerBytes[21] = 0
        headerBytes[22] = 0
        headerBytes[23] = 1

        // e_entry: 0x1040
        headerBytes[24] = 0
        headerBytes[25] = 0
        headerBytes[26] = 0
        headerBytes[27] = 0
        headerBytes[28] = 0
        headerBytes[29] = 0
        headerBytes[30] = 0x10.toByte()
        headerBytes[31] = 0x40.toByte()

        // e_phoff: 64
        headerBytes[32] = 0
        headerBytes[33] = 0
        headerBytes[34] = 0
        headerBytes[35] = 0
        headerBytes[36] = 0
        headerBytes[37] = 0
        headerBytes[38] = 0
        headerBytes[39] = 64

        // e_shoff: 13624
        headerBytes[40] = 0
        headerBytes[41] = 0
        headerBytes[42] = 0
        headerBytes[43] = 0
        headerBytes[44] = 0
        headerBytes[45] = 0
        headerBytes[46] = 0x35.toByte()
        headerBytes[47] = 0x38.toByte()

        // e_flags: 0
        headerBytes[48] = 0
        headerBytes[49] = 0
        headerBytes[50] = 0
        headerBytes[51] = 0

        // e_ehsize: 64
        headerBytes[52] = 0
        headerBytes[53] = 64

        // e_phentsize: 56
        headerBytes[54] = 0
        headerBytes[55] = 56

        // e_phnum: 11
        headerBytes[56] = 0
        headerBytes[57] = 11

        // e_shentsize: 64
        headerBytes[58] = 0
        headerBytes[59] = 64

        // e_shnum: 30
        headerBytes[60] = 0
        headerBytes[61] = 30

        // e_shstrndx: 29
        headerBytes[62] = 0
        headerBytes[63] = 29

        // 测试解析
        val ehdr = ElfEhdr.parse64(headerBytes, 0, ident)

        // 验证解析结果
        assertEquals(true, ehdr.is64Bit)
        assertEquals(ElfType.ET_DYN, ehdr.eType)
        assertEquals(ElfMachine.X86_64, ehdr.eMachine)
        assertEquals(1u, ehdr.eVersion)
        assertEquals(0x1040UL.toAddr(), ehdr.eEntry)
        assertEquals(64UL, ehdr.ePhoff)
        assertEquals(13624UL, ehdr.eShoff)
        assertEquals(0u, ehdr.eFlags)
        assertEquals(64u.toUShort(), ehdr.eEhsize)
        assertEquals(56u.toUShort(), ehdr.ePhentsize)
        assertEquals(11u.toUShort(), ehdr.ePhnum)
        assertEquals(64u.toUShort(), ehdr.eShentsize)
        assertEquals(30u.toUShort(), ehdr.eShnum)
        assertEquals(29u.toUShort(), ehdr.eShstrndx)
    }

    @Test
    fun testElf32PhdrParse() {
        // 创建一个有效的ELF标识
        val identBytes = byteArrayOf(
            0x7F, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(),
            1.toByte(), // ELFCLASS32
            1.toByte(), // ELFDATA2LSB 小端序
            1,
            3.toByte(), // ELFOSABI_LINUX
            0,
            0, 0, 0, 0, 0, 0, 0
        )
        val ident = ElfIdentification.parse(identBytes, 0)

        // 创建一个有效的ELF32程序头部字节数组（小端序）
        val phdrBytes = ByteArray(32) // ELF32程序头部大小为32字节

        // 填充程序头部（小端序）
        // p_type: PT_LOAD (1)
        phdrBytes[0] = 1
        phdrBytes[1] = 0
        phdrBytes[2] = 0
        phdrBytes[3] = 0

        // p_offset: 0
        phdrBytes[4] = 0
        phdrBytes[5] = 0
        phdrBytes[6] = 0
        phdrBytes[7] = 0

        // p_vaddr: 0x8048000
        phdrBytes[8] = 0x00.toByte()
        phdrBytes[9] = 0x80.toByte()
        phdrBytes[10] = 0x04.toByte()
        phdrBytes[11] = 0x08.toByte()

        // p_paddr: 0x8048000
        phdrBytes[12] = 0x00.toByte()
        phdrBytes[13] = 0x80.toByte()
        phdrBytes[14] = 0x04.toByte()
        phdrBytes[15] = 0x08.toByte()

        // p_filesz: 0x1000
        phdrBytes[16] = 0x00.toByte()
        phdrBytes[17] = 0x10.toByte()
        phdrBytes[18] = 0
        phdrBytes[19] = 0

        // p_memsz: 0x1000
        phdrBytes[20] = 0x00.toByte()
        phdrBytes[21] = 0x10.toByte()
        phdrBytes[22] = 0
        phdrBytes[23] = 0

        // p_flags: PF_R | PF_X (5)
        phdrBytes[24] = 5
        phdrBytes[25] = 0
        phdrBytes[26] = 0
        phdrBytes[27] = 0

        // p_align: 0x1000
        phdrBytes[28] = 0x00.toByte()
        phdrBytes[29] = 0x10.toByte()
        phdrBytes[30] = 0
        phdrBytes[31] = 0

        // 测试解析
        val phdr = Elf32Phdr.parse(phdrBytes, 0, ident)

        // 验证解析结果
        assertEquals(ElfPType.PT_LOAD, phdr.pType)
        assertEquals(Elf32Off(0u), phdr.pOffset)
        assertEquals(Elf32Addr(0x8048000u), phdr.pVaddr)
        assertEquals(Elf32Addr(0x8048000u), phdr.pPaddr)
        assertEquals(Elf32Word(0x1000u), phdr.pFilesz)
        assertEquals(Elf32Word(0x1000u), phdr.pMemsz)
        assertEquals(ElfPFlags.PF_R or ElfPFlags.PF_X, phdr.pFlags)
        assertEquals(Elf32Word(0x1000u), phdr.pAlign)
    }

    @Test
    fun testElf64PhdrParse() {
        // 创建一个有效的ELF标识
        val identBytes = byteArrayOf(
            0x7F, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(),
            2.toByte(), // ELFCLASS64
            2.toByte(), // ELFDATA2MSB 大端序
            1,
            9.toByte(), // ELFOSABI_FREEBSD
            0,
            0, 0, 0, 0, 0, 0, 0
        )
        val ident = ElfIdentification.parse(identBytes, 0)

        // 创建一个有效的ELF64程序头部字节数组（大端序）
        val phdrBytes = ByteArray(56) // ELF64程序头部大小为56字节

        // 填充程序头部（大端序）
        // p_type: PT_LOAD (1)
        phdrBytes[0] = 0
        phdrBytes[1] = 0
        phdrBytes[2] = 0
        phdrBytes[3] = 1

        // p_flags: PF_R | PF_X (5)
        phdrBytes[4] = 0
        phdrBytes[5] = 0
        phdrBytes[6] = 0
        phdrBytes[7] = 5

        // p_offset: 0
        phdrBytes[8] = 0
        phdrBytes[9] = 0
        phdrBytes[10] = 0
        phdrBytes[11] = 0
        phdrBytes[12] = 0
        phdrBytes[13] = 0
        phdrBytes[14] = 0
        phdrBytes[15] = 0

        // p_vaddr: 0x400000 (大端序)
        phdrBytes[16] = 0
        phdrBytes[17] = 0
        phdrBytes[18] = 0
        phdrBytes[19] = 0
        phdrBytes[20] = 0
        phdrBytes[21] = 0x40.toByte()
        phdrBytes[22] = 0
        phdrBytes[23] = 0

        // p_paddr: 0x400000 (大端序)
        phdrBytes[24] = 0
        phdrBytes[25] = 0
        phdrBytes[26] = 0
        phdrBytes[27] = 0
        phdrBytes[28] = 0
        phdrBytes[29] = 0x40.toByte()
        phdrBytes[30] = 0
        phdrBytes[31] = 0

        // p_filesz: 0x1000 (大端序)
        phdrBytes[32] = 0
        phdrBytes[33] = 0
        phdrBytes[34] = 0
        phdrBytes[35] = 0
        phdrBytes[36] = 0
        phdrBytes[37] = 0
        phdrBytes[38] = 0x10.toByte()
        phdrBytes[39] = 0

        // p_memsz: 0x1000 (大端序)
        phdrBytes[40] = 0
        phdrBytes[41] = 0
        phdrBytes[42] = 0
        phdrBytes[43] = 0
        phdrBytes[44] = 0
        phdrBytes[45] = 0
        phdrBytes[46] = 0x10.toByte()
        phdrBytes[47] = 0

        // p_align: 0x1000 (大端序)
        phdrBytes[48] = 0
        phdrBytes[49] = 0
        phdrBytes[50] = 0
        phdrBytes[51] = 0
        phdrBytes[52] = 0
        phdrBytes[53] = 0
        phdrBytes[54] = 0x10.toByte()
        phdrBytes[55] = 0

        // 测试解析
        val phdr = Elf64Phdr.parse(phdrBytes, 0, ident)

        // 验证解析结果
        assertEquals(ElfPType.PT_LOAD, phdr.pType)
        assertEquals(ElfPFlags.PF_R or ElfPFlags.PF_X, phdr.pFlags)
        assertEquals(Elf64Off(0u), phdr.pOffset)
        assertEquals(Elf64Addr(0x400000u), phdr.pVaddr)
        assertEquals(Elf64Addr(0x400000u), phdr.pPaddr)
        assertEquals(Elf64Xword(0x1000UL), phdr.pFilesz)
        assertEquals(Elf64Xword(0x1000UL), phdr.pMemsz)
        assertEquals(Elf64Xword(0x1000UL), phdr.pAlign)
    }

    @Test
    fun testElf32ShdrParse() {
        // 创建一个有效的节头部字节数组（小端序）
        val shdrBytes = ByteArray(40) // ELF32节头部大小为40字节

        // 填充节头部（小端序）
        // sh_name: 1
        shdrBytes[0] = 1
        shdrBytes[1] = 0
        shdrBytes[2] = 0
        shdrBytes[3] = 0

        // sh_type: SHT_PROGBITS (1)
        shdrBytes[4] = 1
        shdrBytes[5] = 0
        shdrBytes[6] = 0
        shdrBytes[7] = 0

        // sh_flags: SHF_ALLOC | SHF_EXECINSTR (6)
        shdrBytes[8] = 6
        shdrBytes[9] = 0
        shdrBytes[10] = 0
        shdrBytes[11] = 0

        // sh_addr: 0x8048000
        shdrBytes[12] = 0x00.toByte()
        shdrBytes[13] = 0x80.toByte()
        shdrBytes[14] = 0x04.toByte()
        shdrBytes[15] = 0x08.toByte()

        // sh_offset: 0x1000
        shdrBytes[16] = 0x00.toByte()
        shdrBytes[17] = 0x10.toByte()
        shdrBytes[18] = 0
        shdrBytes[19] = 0

        // sh_size: 0x2000
        shdrBytes[20] = 0x00.toByte()
        shdrBytes[21] = 0x20.toByte()
        shdrBytes[22] = 0
        shdrBytes[23] = 0

        // sh_link: 0
        shdrBytes[24] = 0
        shdrBytes[25] = 0
        shdrBytes[26] = 0
        shdrBytes[27] = 0

        // sh_info: 0
        shdrBytes[28] = 0
        shdrBytes[29] = 0
        shdrBytes[30] = 0
        shdrBytes[31] = 0

        // sh_addralign: 4
        shdrBytes[32] = 4
        shdrBytes[33] = 0
        shdrBytes[34] = 0
        shdrBytes[35] = 0

        // sh_entsize: 0
        shdrBytes[36] = 0
        shdrBytes[37] = 0
        shdrBytes[38] = 0
        shdrBytes[39] = 0

        // 测试解析
        val shdr = Elf32Shdr.parse(shdrBytes, 0, true) // true表示小端序

        // 验证解析结果
        assertEquals(Elf32Word(1u), shdr.shName)
        assertEquals(ElfSType.SHT_PROGBITS, shdr.shType)
        assertEquals(ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR, shdr.shFlags)
        assertEquals(Elf32Addr(0x8048000u), shdr.shAddr)
        assertEquals(Elf32Off(0x1000u), shdr.shOffset)
        assertEquals(Elf32Word(0x2000u), shdr.shSize)
        assertEquals(Elf32Word(0u), shdr.shLink)
        assertEquals(Elf32Word(0u), shdr.shInfo)
        assertEquals(Elf32Word(4u), shdr.shAddralign)
        assertEquals(Elf32Word(0u), shdr.shEntsize)
    }

    @Test
    fun testElf64ShdrParse() {
        // 创建一个有效的节头部字节数组（大端序）
        val shdrBytes = ByteArray(64) // ELF64节头部大小为64字节

        // 填充节头部（大端序）
        // sh_name: 1
        shdrBytes[0] = 0
        shdrBytes[1] = 0
        shdrBytes[2] = 0
        shdrBytes[3] = 1

        // sh_type: SHT_PROGBITS (1)
        shdrBytes[4] = 0
        shdrBytes[5] = 0
        shdrBytes[6] = 0
        shdrBytes[7] = 1

        // sh_flags: SHF_ALLOC | SHF_EXECINSTR (6)
        shdrBytes[8] = 0
        shdrBytes[9] = 0
        shdrBytes[10] = 0
        shdrBytes[11] = 0
        shdrBytes[12] = 0
        shdrBytes[13] = 0
        shdrBytes[14] = 0
        shdrBytes[15] = 6

        // sh_addr: 0x400000 (大端序)
        shdrBytes[16] = 0
        shdrBytes[17] = 0
        shdrBytes[18] = 0
        shdrBytes[19] = 0
        shdrBytes[20] = 0
        shdrBytes[21] = 0x40.toByte()
        shdrBytes[22] = 0
        shdrBytes[23] = 0

        // sh_offset: 0x1000 (大端序)
        shdrBytes[24] = 0
        shdrBytes[25] = 0
        shdrBytes[26] = 0
        shdrBytes[27] = 0
        shdrBytes[28] = 0
        shdrBytes[29] = 0
        shdrBytes[30] = 0x10.toByte()
        shdrBytes[31] = 0

        // sh_size: 0x2000 (大端序)
        shdrBytes[32] = 0
        shdrBytes[33] = 0
        shdrBytes[34] = 0
        shdrBytes[35] = 0
        shdrBytes[36] = 0
        shdrBytes[37] = 0
        shdrBytes[38] = 0x20.toByte()
        shdrBytes[39] = 0

        // sh_link: 0
        shdrBytes[40] = 0
        shdrBytes[41] = 0
        shdrBytes[42] = 0
        shdrBytes[43] = 0

        // sh_info: 0
        shdrBytes[44] = 0
        shdrBytes[45] = 0
        shdrBytes[46] = 0
        shdrBytes[47] = 0

        // sh_addralign: 8 (大端序)
        shdrBytes[48] = 0
        shdrBytes[49] = 0
        shdrBytes[50] = 0
        shdrBytes[51] = 0
        shdrBytes[52] = 0
        shdrBytes[53] = 0
        shdrBytes[54] = 0
        shdrBytes[55] = 8

        // sh_entsize: 0
        shdrBytes[56] = 0
        shdrBytes[57] = 0
        shdrBytes[58] = 0
        shdrBytes[59] = 0
        shdrBytes[60] = 0
        shdrBytes[61] = 0
        shdrBytes[62] = 0
        shdrBytes[63] = 0

        // 测试解析
        val shdr = Elf64Shdr.parse(shdrBytes, 0, false) // false表示大端序

        // 验证解析结果
        assertEquals(Elf64Word(1u), shdr.shName)
        assertEquals(ElfSType.SHT_PROGBITS, shdr.shType)
        assertEquals(ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR, shdr.shFlags)
        assertEquals(Elf64Addr(0x400000u), shdr.shAddr)
        assertEquals(Elf64Off(0x1000u), shdr.shOffset)
        assertEquals(Elf64Xword(0x2000UL), shdr.shSize)
        assertEquals(Elf64Word(0u), shdr.shLink)
        assertEquals(Elf64Word(0u), shdr.shInfo)
        assertEquals(Elf64Xword(8UL), shdr.shAddralign)
        assertEquals(Elf64Xword(0UL), shdr.shEntsize)
    }
} 