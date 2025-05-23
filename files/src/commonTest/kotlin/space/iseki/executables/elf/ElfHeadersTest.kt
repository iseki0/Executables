package space.iseki.executables.elf

import kotlinx.serialization.json.Json
import space.iseki.executables.common.Address32
import space.iseki.executables.common.Address64
import space.iseki.executables.common.toAddr
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 测试ElfEhdr、ElfPhdr及其子类的序列化和反序列化功能
 */
class ElfHeadersTest {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun testElf32EhdrSerialization() {
        // 创建一个32位ElfEhdr实例
        val original = ElfEhdr(
            is64Bit = false,
            eType = ElfType.ET_EXEC,
            eMachine = ElfMachine.I386,
            eVersion = 1u,
            eEntry = 0x8048000UL.toAddr(),
            ePhoff = 52UL,
            eShoff = 2104UL,
            eFlags = 0u,
            eEhsize = 52u.toUShort(),
            ePhentsize = 32u.toUShort(),
            ePhnum = 8u.toUShort(),
            eShentsize = 40u.toUShort(),
            eShnum = 25u.toUShort(),
            eShstrndx = 24u.toUShort()
        )

        // 序列化为JSON
        val serialized = json.encodeToString<ElfEhdr>(original)
        println("Elf32Ehdr序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfEhdr>(serialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(false, deserialized.is64Bit)
        assertEquals(ElfType.ET_EXEC, deserialized.eType)
        assertEquals(ElfMachine.I386, deserialized.eMachine)
        assertEquals(1u, deserialized.eVersion)
        assertEquals(0x8048000UL.toAddr(), deserialized.eEntry)
        assertEquals(52UL, deserialized.ePhoff)
        assertEquals(2104UL, deserialized.eShoff)
        assertEquals(0u, deserialized.eFlags)
        assertEquals(52u.toUShort(), deserialized.eEhsize)
        assertEquals(32u.toUShort(), deserialized.ePhentsize)
        assertEquals(8u.toUShort(), deserialized.ePhnum)
        assertEquals(40u.toUShort(), deserialized.eShentsize)
        assertEquals(25u.toUShort(), deserialized.eShnum)
        assertEquals(24u.toUShort(), deserialized.eShstrndx)
    }

    @Test
    fun testElf64EhdrSerialization() {
        // 创建一个64位ElfEhdr实例
        val original = ElfEhdr(
            is64Bit = true,
            eType = ElfType.ET_DYN,
            eMachine = ElfMachine.X86_64,
            eVersion = 1u,
            eEntry = 0x1040UL.toAddr(),
            ePhoff = 64UL,
            eShoff = 13624UL,
            eFlags = 0u,
            eEhsize = 64u.toUShort(),
            ePhentsize = 56u.toUShort(),
            ePhnum = 11u.toUShort(),
            eShentsize = 64u.toUShort(),
            eShnum = 30u.toUShort(),
            eShstrndx = 29u.toUShort()
        )

        // 序列化为JSON
        val serialized = json.encodeToString<ElfEhdr>(original)
        println("Elf64Ehdr序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfEhdr>(serialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(true, deserialized.is64Bit)
        assertEquals(ElfType.ET_DYN, deserialized.eType)
        assertEquals(ElfMachine.X86_64, deserialized.eMachine)
        assertEquals(1u, deserialized.eVersion)
        assertEquals(0x1040UL.toAddr(), deserialized.eEntry)
        assertEquals(64UL, deserialized.ePhoff)
        assertEquals(13624UL, deserialized.eShoff)
        assertEquals(0u, deserialized.eFlags)
        assertEquals(64u.toUShort(), deserialized.eEhsize)
        assertEquals(56u.toUShort(), deserialized.ePhentsize)
        assertEquals(11u.toUShort(), deserialized.ePhnum)
        assertEquals(64u.toUShort(), deserialized.eShentsize)
        assertEquals(30u.toUShort(), deserialized.eShnum)
        assertEquals(29u.toUShort(), deserialized.eShstrndx)
    }

    @Test
    fun testElf32PhdrSerialization() {
        // 创建一个32位ElfPhdr实例
        val original = ElfPhdr(
            is64Bit = false,
            pType = ElfPType.PT_LOAD,
            pFlags = ElfPFlags.PF_R or ElfPFlags.PF_X,
            pOffset = 0uL,
            pVaddr = Address64(0x8048000uL),
            pPaddr = Address64(0x8048000uL),
            pFilesz = 0x1000uL,
            pMemsz = 0x1000uL,
            pAlign = 0x1000uL
        )

        // 序列化为JSON
        val serialized = json.encodeToString(original)
        println("Elf32Phdr序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfPhdr>(serialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(false, deserialized.is64Bit)
        assertEquals(ElfPType.PT_LOAD, deserialized.pType)
        assertEquals(0uL, deserialized.pOffset)
        assertEquals(Address64(0x8048000uL), deserialized.pVaddr)
        assertEquals(Address64(0x8048000uL), deserialized.pPaddr)
        assertEquals(0x1000uL, deserialized.pFilesz)
        assertEquals(0x1000uL, deserialized.pMemsz)
        assertEquals(ElfPFlags.PF_R or ElfPFlags.PF_X, deserialized.pFlags)
        assertEquals(0x1000uL, deserialized.pAlign)
    }

    @Test
    fun testElf64PhdrSerialization() {
        // 创建一个64位ElfPhdr实例
        val original = ElfPhdr(
            is64Bit = true,
            pType = ElfPType.PT_LOAD,
            pFlags = ElfPFlags.PF_R or ElfPFlags.PF_X,
            pOffset = 0uL,
            pVaddr = Address64(0x400000uL),
            pPaddr = Address64(0x400000uL),
            pFilesz = 0x1000uL,
            pMemsz = 0x1000uL,
            pAlign = 0x1000uL
        )

        // 序列化为JSON
        val serialized = json.encodeToString(original)
        println("Elf64Phdr序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfPhdr>(serialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(true, deserialized.is64Bit)
        assertEquals(ElfPType.PT_LOAD, deserialized.pType)
        assertEquals(ElfPFlags.PF_R or ElfPFlags.PF_X, deserialized.pFlags)
        assertEquals(0uL, deserialized.pOffset)
        assertEquals(Address64(0x400000uL), deserialized.pVaddr)
        assertEquals(Address64(0x400000uL), deserialized.pPaddr)
        assertEquals(0x1000uL, deserialized.pFilesz)
        assertEquals(0x1000uL, deserialized.pMemsz)
        assertEquals(0x1000uL, deserialized.pAlign)
    }

    @Test
    fun testElfEhdrFields() {
        // 创建32位和64位ElfEhdr实例
        val elf32Ehdr = ElfEhdr(
            is64Bit = false,
            eType = ElfType.ET_EXEC,
            eMachine = ElfMachine.I386,
            eVersion = 1u,
            eEntry = 0x8048000UL.toAddr(),
            ePhoff = 52UL,
            eShoff = 2104UL,
            eFlags = 0u,
            eEhsize = 52u.toUShort(),
            ePhentsize = 32u.toUShort(),
            ePhnum = 8u.toUShort(),
            eShentsize = 40u.toUShort(),
            eShnum = 25u.toUShort(),
            eShstrndx = 24u.toUShort()
        )

        val elf64Ehdr = ElfEhdr(
            is64Bit = true,
            eType = ElfType.ET_DYN,
            eMachine = ElfMachine.X86_64,
            eVersion = 1u,
            eEntry = 0x1040UL.toAddr(),
            ePhoff = 64UL,
            eShoff = 13624UL,
            eFlags = 0u,
            eEhsize = 64u.toUShort(),
            ePhentsize = 56u.toUShort(),
            ePhnum = 11u.toUShort(),
            eShentsize = 64u.toUShort(),
            eShnum = 30u.toUShort(),
            eShstrndx = 29u.toUShort()
        )

        // 验证32位ElfEhdr的fields映射
        val fields32 = elf32Ehdr.fields
        assertEquals(14, fields32.size) // 包含 is64Bit 字段
        assertEquals(false, fields32["is64Bit"])
        assertEquals(ElfType.ET_EXEC, fields32["eType"])
        assertEquals(ElfMachine.I386, fields32["eMachine"])
        assertEquals(1u, fields32["eVersion"])
        assertEquals(0x8048000u.toAddr(), fields32["eEntry"])
        assertEquals(52u, fields32["ePhoff"])
        assertEquals(2104u, fields32["eShoff"])
        assertEquals(0u, fields32["eFlags"])
        assertEquals(52u.toUShort(), fields32["eEhsize"])
        assertEquals(32u.toUShort(), fields32["ePhentsize"])
        assertEquals(8u.toUShort(), fields32["ePhnum"])
        assertEquals(40u.toUShort(), fields32["eShentsize"])
        assertEquals(25u.toUShort(), fields32["eShnum"])
        assertEquals(24u.toUShort(), fields32["eShstrndx"])

        // 验证64位ElfEhdr的fields映射
        val fields64 = elf64Ehdr.fields
        assertEquals(14, fields64.size) // 包含 is64Bit 字段
        assertEquals(true, fields64["is64Bit"])
        assertEquals(ElfType.ET_DYN, fields64["eType"])
        assertEquals(ElfMachine.X86_64, fields64["eMachine"])
        assertEquals(1u, fields64["eVersion"])
        assertEquals(0x1040uL.toAddr(), fields64["eEntry"])
        assertEquals(64uL, fields64["ePhoff"])
        assertEquals(13624uL, fields64["eShoff"])
        assertEquals(0u, fields64["eFlags"])
        assertEquals(64u.toUShort(), fields64["eEhsize"])
        assertEquals(56u.toUShort(), fields64["ePhentsize"])
        assertEquals(11u.toUShort(), fields64["ePhnum"])
        assertEquals(64u.toUShort(), fields64["eShentsize"])
        assertEquals(30u.toUShort(), fields64["eShnum"])
        assertEquals(29u.toUShort(), fields64["eShstrndx"])
    }

    @Test
    fun testElfPhdrFields() {
        // 创建32位和64位ElfPhdr实例
        val elf32Phdr = ElfPhdr(
            is64Bit = false,
            pType = ElfPType.PT_LOAD,
            pFlags = ElfPFlags.PF_R or ElfPFlags.PF_X,
            pOffset = 0uL,
            pVaddr = Address64(0x8048000uL),
            pPaddr = Address64(0x8048000uL),
            pFilesz = 0x1000uL,
            pMemsz = 0x1000uL,
            pAlign = 0x1000uL
        )

        val elf64Phdr = ElfPhdr(
            is64Bit = true,
            pType = ElfPType.PT_LOAD,
            pFlags = ElfPFlags.PF_R or ElfPFlags.PF_X,
            pOffset = 0uL,
            pVaddr = Address64(0x400000uL),
            pPaddr = Address64(0x400000uL),
            pFilesz = 0x1000uL,
            pMemsz = 0x1000uL,
            pAlign = 0x1000uL
        )

        // 验证32位ElfPhdr的fields映射
        val fields32 = elf32Phdr.fields
        assertEquals(9, fields32.size)  // 包含is64Bit字段
        assertEquals(false, fields32["is64Bit"])
        assertEquals(ElfPType.PT_LOAD, fields32["pType"])
        assertEquals(0u, fields32["pOffset"])
        assertEquals(Address32(0x8048000u), fields32["pVaddr"])
        assertEquals(Address32(0x8048000u), fields32["pPaddr"])
        assertEquals(0x1000u, fields32["pFilesz"])
        assertEquals(0x1000u, fields32["pMemsz"])
        assertEquals(ElfPFlags.PF_R or ElfPFlags.PF_X, fields32["pFlags"])
        assertEquals(0x1000u, fields32["pAlign"])

        // 验证64位ElfPhdr的fields映射
        val fields64 = elf64Phdr.fields
        assertEquals(9, fields64.size)  // 包含is64Bit字段
        assertEquals(true, fields64["is64Bit"])
        assertEquals(ElfPType.PT_LOAD, fields64["pType"])
        assertEquals(ElfPFlags.PF_R or ElfPFlags.PF_X, fields64["pFlags"])
        assertEquals(0uL, fields64["pOffset"])
        assertEquals(Address64(0x400000uL), fields64["pVaddr"])
        assertEquals(Address64(0x400000uL), fields64["pPaddr"])
        assertEquals(0x1000uL, fields64["pFilesz"])
        assertEquals(0x1000uL, fields64["pMemsz"])
        assertEquals(0x1000uL, fields64["pAlign"])
    }
} 