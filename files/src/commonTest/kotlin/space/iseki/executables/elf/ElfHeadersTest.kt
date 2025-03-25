package space.iseki.executables.elf

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
        // 创建一个Elf32Ehdr实例
        val original = Elf32Ehdr(
            eType = ElfType.ET_EXEC,
            eMachine = ElfMachine.I386,
            eVersion = Elf32Word(1u),
            eEntry = Elf32Addr(0x8048000u),
            ePhoff = Elf32Off(52u),
            eShoff = Elf32Off(2104u),
            eFlags = Elf32Word(0u),
            eEhsize = Elf32Half(52u),
            ePhentsize = Elf32Half(32u),
            ePhnum = Elf32Half(8u),
            eShentsize = Elf32Half(40u),
            eShnum = Elf32Half(25u),
            eShstrndx = Elf32Half(24u)
        )

        // 序列化为JSON
        val serialized = json.encodeToString<ElfEhdr>(original)
        println("Elf32Ehdr序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfEhdr>(serialized)

        // 验证反序列化的对象是Elf32Ehdr类型
        assertIs<Elf32Ehdr>(deserialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(ElfType.ET_EXEC, deserialized.eType)
        assertEquals(ElfMachine.I386, deserialized.eMachine)
        assertEquals(Elf32Word(1u), deserialized.eVersion)
        assertEquals(Elf32Addr(0x8048000u), deserialized.eEntry)
        assertEquals(Elf32Off(52u), deserialized.ePhoff)
        assertEquals(Elf32Off(2104u), deserialized.eShoff)
        assertEquals(Elf32Word(0u), deserialized.eFlags)
        assertEquals(Elf32Half(52u), deserialized.eEhsize)
        assertEquals(Elf32Half(32u), deserialized.ePhentsize)
        assertEquals(Elf32Half(8u), deserialized.ePhnum)
        assertEquals(Elf32Half(40u), deserialized.eShentsize)
        assertEquals(Elf32Half(25u), deserialized.eShnum)
        assertEquals(Elf32Half(24u), deserialized.eShstrndx)
    }

    @Test
    fun testElf64EhdrSerialization() {
        // 创建一个Elf64Ehdr实例
        val original = Elf64Ehdr(
            eType = ElfType.ET_DYN,
            eMachine = ElfMachine.X86_64,
            eVersion = Elf64Word(1u),
            eEntry = Elf64Addr(0x1040u),
            ePhoff = Elf64Off(64u),
            eShoff = Elf64Off(13624u),
            eFlags = Elf64Word(0u),
            eEhsize = Elf64Half(64u),
            ePhentsize = Elf64Half(56u),
            ePhnum = Elf64Half(11u),
            eShentsize = Elf64Half(64u),
            eShnum = Elf64Half(30u),
            eShstrndx = Elf64Half(29u)
        )

        // 序列化为JSON
        val serialized = json.encodeToString<ElfEhdr>(original)
        println("Elf64Ehdr序列化结果: $serialized")

        // 从JSON反序列化

        val deserialized = json.decodeFromString<ElfEhdr>(serialized)

        // 验证反序列化的对象是Elf64Ehdr类型
        assertIs<Elf64Ehdr>(deserialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(ElfType.ET_DYN, deserialized.eType)
        assertEquals(ElfMachine.X86_64, deserialized.eMachine)
        assertEquals(Elf64Word(1u), deserialized.eVersion)
        assertEquals(Elf64Addr(0x1040u), deserialized.eEntry)
        assertEquals(Elf64Off(64u), deserialized.ePhoff)
        assertEquals(Elf64Off(13624u), deserialized.eShoff)
        assertEquals(Elf64Word(0u), deserialized.eFlags)
        assertEquals(Elf64Half(64u), deserialized.eEhsize)
        assertEquals(Elf64Half(56u), deserialized.ePhentsize)
        assertEquals(Elf64Half(11u), deserialized.ePhnum)
        assertEquals(Elf64Half(64u), deserialized.eShentsize)
        assertEquals(Elf64Half(30u), deserialized.eShnum)
        assertEquals(Elf64Half(29u), deserialized.eShstrndx)
    }

    @Test
    fun testElf32PhdrSerialization() {
        // 创建一个Elf32Phdr实例
        val original = Elf32Phdr(
            pType = ElfPType.PT_LOAD,
            pOffset = Elf32Off(0u),
            pVaddr = Elf32Addr(0x8048000u),
            pPaddr = Elf32Addr(0x8048000u),
            pFilesz = Elf32Word(0x1000u),
            pMemsz = Elf32Word(0x1000u),
            pFlags = ElfPFlags.PF_R or ElfPFlags.PF_X,
            pAlign = Elf32Word(0x1000u)
        )

        // 序列化为JSON
        val serialized = json.encodeToString<ElfPhdr>(original)
        println("Elf32Phdr序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfPhdr>(serialized)

        // 验证反序列化的对象是Elf32Phdr类型
        assertIs<Elf32Phdr>(deserialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(ElfPType.PT_LOAD, deserialized.pType)
        assertEquals(Elf32Off(0u), deserialized.pOffset)
        assertEquals(Elf32Addr(0x8048000u), deserialized.pVaddr)
        assertEquals(Elf32Addr(0x8048000u), deserialized.pPaddr)
        assertEquals(Elf32Word(0x1000u), deserialized.pFilesz)
        assertEquals(Elf32Word(0x1000u), deserialized.pMemsz)
        assertEquals(ElfPFlags.PF_R or ElfPFlags.PF_X, deserialized.pFlags)
        assertEquals(Elf32Word(0x1000u), deserialized.pAlign)
    }

    @Test
    fun testElf64PhdrSerialization() {
        // 创建一个Elf64Phdr实例
        val original = Elf64Phdr(
            pType = ElfPType.PT_LOAD,
            pFlags = ElfPFlags.PF_R or ElfPFlags.PF_X,
            pOffset = Elf64Off(0u),
            pVaddr = Elf64Addr(0x400000u),
            pPaddr = Elf64Addr(0x400000u),
            pFilesz = Elf64Xword(0x1000UL),
            pMemsz = Elf64Xword(0x1000UL),
            pAlign = Elf64Xword(0x1000UL)
        )

        // 序列化为JSON
        val serialized = json.encodeToString<ElfPhdr>(original)
        println("Elf64Phdr序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfPhdr>(serialized)

        // 验证反序列化的对象是Elf64Phdr类型
        assertIs<Elf64Phdr>(deserialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(ElfPType.PT_LOAD, deserialized.pType)
        assertEquals(ElfPFlags.PF_R or ElfPFlags.PF_X, deserialized.pFlags)
        assertEquals(Elf64Off(0u), deserialized.pOffset)
        assertEquals(Elf64Addr(0x400000u), deserialized.pVaddr)
        assertEquals(Elf64Addr(0x400000u), deserialized.pPaddr)
        assertEquals(Elf64Xword(0x1000UL), deserialized.pFilesz)
        assertEquals(Elf64Xword(0x1000UL), deserialized.pMemsz)
        assertEquals(Elf64Xword(0x1000UL), deserialized.pAlign)
    }

    @Test
    fun testElfEhdrPolymorphicSerialization() {
        // 创建两个不同类型的ElfEhdr实例
        val elf32Ehdr: ElfEhdr = Elf32Ehdr(
            eType = ElfType.ET_EXEC,
            eMachine = ElfMachine.I386,
            eVersion = Elf32Word(1u),
            eEntry = Elf32Addr(0x8048000u),
            ePhoff = Elf32Off(52u),
            eShoff = Elf32Off(2104u),
            eFlags = Elf32Word(0u),
            eEhsize = Elf32Half(52u),
            ePhentsize = Elf32Half(32u),
            ePhnum = Elf32Half(8u),
            eShentsize = Elf32Half(40u),
            eShnum = Elf32Half(25u),
            eShstrndx = Elf32Half(24u)
        )

        val elf64Ehdr: ElfEhdr = Elf64Ehdr(
            eType = ElfType.ET_DYN,
            eMachine = ElfMachine.X86_64,
            eVersion = Elf64Word(1u),
            eEntry = Elf64Addr(0x1040u),
            ePhoff = Elf64Off(64u),
            eShoff = Elf64Off(13624u),
            eFlags = Elf64Word(0u),
            eEhsize = Elf64Half(64u),
            ePhentsize = Elf64Half(56u),
            ePhnum = Elf64Half(11u),
            eShentsize = Elf64Half(64u),
            eShnum = Elf64Half(30u),
            eShstrndx = Elf64Half(29u)
        )

        // 序列化为JSON
        val serialized32 = json.encodeToString<ElfEhdr>(elf32Ehdr)
        val serialized64 = json.encodeToString<ElfEhdr>(elf64Ehdr)

        println("Elf32Ehdr多态序列化结果: $serialized32")
        println("Elf64Ehdr多态序列化结果: $serialized64")

        // 从JSON反序列化
        val deserialized32 = json.decodeFromString<ElfEhdr>(serialized32)
        val deserialized64 = json.decodeFromString<ElfEhdr>(serialized64)

        // 验证反序列化的对象类型
        assertIs<Elf32Ehdr>(deserialized32)
        assertIs<Elf64Ehdr>(deserialized64)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(elf32Ehdr, deserialized32)
        assertEquals(elf64Ehdr, deserialized64)
    }

    @Test
    fun testElfPhdrPolymorphicSerialization() {
        // 创建两个不同类型的ElfPhdr实例
        val elf32Phdr: ElfPhdr = Elf32Phdr(
            pType = ElfPType.PT_LOAD,
            pOffset = Elf32Off(0u),
            pVaddr = Elf32Addr(0x8048000u),
            pPaddr = Elf32Addr(0x8048000u),
            pFilesz = Elf32Word(0x1000u),
            pMemsz = Elf32Word(0x1000u),
            pFlags = ElfPFlags.PF_R or ElfPFlags.PF_X,
            pAlign = Elf32Word(0x1000u)
        )

        val elf64Phdr: ElfPhdr = Elf64Phdr(
            pType = ElfPType.PT_LOAD,
            pFlags = ElfPFlags.PF_R or ElfPFlags.PF_X,
            pOffset = Elf64Off(0u),
            pVaddr = Elf64Addr(0x400000u),
            pPaddr = Elf64Addr(0x400000u),
            pFilesz = Elf64Xword(0x1000UL),
            pMemsz = Elf64Xword(0x1000UL),
            pAlign = Elf64Xword(0x1000UL)
        )

        // 序列化为JSON
        val serialized32 = json.encodeToString<ElfPhdr>(elf32Phdr)
        val serialized64 = json.encodeToString<ElfPhdr>(elf64Phdr)

        println("Elf32Phdr多态序列化结果: $serialized32")
        println("Elf64Phdr多态序列化结果: $serialized64")

        // 从JSON反序列化
        val deserialized32 = json.decodeFromString<ElfPhdr>(serialized32)
        val deserialized64 = json.decodeFromString<ElfPhdr>(serialized64)

        // 验证反序列化的对象类型
        assertIs<Elf32Phdr>(deserialized32)
        assertIs<Elf64Phdr>(deserialized64)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(elf32Phdr, deserialized32)
        assertEquals(elf64Phdr, deserialized64)
    }

    @Test
    fun testElfEhdrFields() {
        // 创建Elf32Ehdr和Elf64Ehdr实例
        val elf32Ehdr = Elf32Ehdr(
            eType = ElfType.ET_EXEC,
            eMachine = ElfMachine.I386,
            eVersion = Elf32Word(1u),
            eEntry = Elf32Addr(0x8048000u),
            ePhoff = Elf32Off(52u),
            eShoff = Elf32Off(2104u),
            eFlags = Elf32Word(0u),
            eEhsize = Elf32Half(52u),
            ePhentsize = Elf32Half(32u),
            ePhnum = Elf32Half(8u),
            eShentsize = Elf32Half(40u),
            eShnum = Elf32Half(25u),
            eShstrndx = Elf32Half(24u)
        )

        val elf64Ehdr = Elf64Ehdr(
            eType = ElfType.ET_DYN,
            eMachine = ElfMachine.X86_64,
            eVersion = Elf64Word(1u),
            eEntry = Elf64Addr(0x1040u),
            ePhoff = Elf64Off(64u),
            eShoff = Elf64Off(13624u),
            eFlags = Elf64Word(0u),
            eEhsize = Elf64Half(64u),
            ePhentsize = Elf64Half(56u),
            ePhnum = Elf64Half(11u),
            eShentsize = Elf64Half(64u),
            eShnum = Elf64Half(30u),
            eShstrndx = Elf64Half(29u)
        )

        // 验证Elf32Ehdr的fields映射
        val fields32 = elf32Ehdr.fields
        assertEquals(13, fields32.size)
        assertEquals(ElfType.ET_EXEC, fields32["eType"])
        assertEquals(ElfMachine.I386, fields32["eMachine"])
        assertEquals(Elf32Word(1u), fields32["eVersion"])
        assertEquals(Elf32Addr(0x8048000u), fields32["eEntry"])
        assertEquals(Elf32Off(52u), fields32["ePhoff"])
        assertEquals(Elf32Off(2104u), fields32["eShoff"])
        assertEquals(Elf32Word(0u), fields32["eFlags"])
        assertEquals(Elf32Half(52u), fields32["eEhsize"])
        assertEquals(Elf32Half(32u), fields32["ePhentsize"])
        assertEquals(Elf32Half(8u), fields32["ePhnum"])
        assertEquals(Elf32Half(40u), fields32["eShentsize"])
        assertEquals(Elf32Half(25u), fields32["eShnum"])
        assertEquals(Elf32Half(24u), fields32["eShstrndx"])

        // 验证Elf64Ehdr的fields映射
        val fields64 = elf64Ehdr.fields
        assertEquals(13, fields64.size)
        assertEquals(ElfType.ET_DYN, fields64["eType"])
        assertEquals(ElfMachine.X86_64, fields64["eMachine"])
        assertEquals(Elf64Word(1u), fields64["eVersion"])
        assertEquals(Elf64Addr(0x1040u), fields64["eEntry"])
        assertEquals(Elf64Off(64u), fields64["ePhoff"])
        assertEquals(Elf64Off(13624u), fields64["eShoff"])
        assertEquals(Elf64Word(0u), fields64["eFlags"])
        assertEquals(Elf64Half(64u), fields64["eEhsize"])
        assertEquals(Elf64Half(56u), fields64["ePhentsize"])
        assertEquals(Elf64Half(11u), fields64["ePhnum"])
        assertEquals(Elf64Half(64u), fields64["eShentsize"])
        assertEquals(Elf64Half(30u), fields64["eShnum"])
        assertEquals(Elf64Half(29u), fields64["eShstrndx"])
    }

    @Test
    fun testElfPhdrFields() {
        // 创建Elf32Phdr和Elf64Phdr实例
        val elf32Phdr = Elf32Phdr(
            pType = ElfPType.PT_LOAD,
            pOffset = Elf32Off(0u),
            pVaddr = Elf32Addr(0x8048000u),
            pPaddr = Elf32Addr(0x8048000u),
            pFilesz = Elf32Word(0x1000u),
            pMemsz = Elf32Word(0x1000u),
            pFlags = ElfPFlags.PF_R or ElfPFlags.PF_X,
            pAlign = Elf32Word(0x1000u)
        )

        val elf64Phdr = Elf64Phdr(
            pType = ElfPType.PT_LOAD,
            pFlags = ElfPFlags.PF_R or ElfPFlags.PF_X,
            pOffset = Elf64Off(0u),
            pVaddr = Elf64Addr(0x400000u),
            pPaddr = Elf64Addr(0x400000u),
            pFilesz = Elf64Xword(0x1000UL),
            pMemsz = Elf64Xword(0x1000UL),
            pAlign = Elf64Xword(0x1000UL)
        )

        // 验证Elf32Phdr的fields映射
        val fields32 = elf32Phdr.fields
        assertEquals(8, fields32.size)
        assertEquals(ElfPType.PT_LOAD, fields32["pType"])
        assertEquals(Elf32Off(0u), fields32["pOffset"])
        assertEquals(Elf32Addr(0x8048000u), fields32["pVaddr"])
        assertEquals(Elf32Addr(0x8048000u), fields32["pPaddr"])
        assertEquals(Elf32Word(0x1000u), fields32["pFilesz"])
        assertEquals(Elf32Word(0x1000u), fields32["pMemsz"])
        assertEquals(ElfPFlags.PF_R or ElfPFlags.PF_X, fields32["pFlags"])
        assertEquals(Elf32Word(0x1000u), fields32["pAlign"])

        // 验证Elf64Phdr的fields映射
        val fields64 = elf64Phdr.fields
        assertEquals(8, fields64.size)
        assertEquals(ElfPType.PT_LOAD, fields64["pType"])
        assertEquals(ElfPFlags.PF_R or ElfPFlags.PF_X, fields64["pFlags"])
        assertEquals(Elf64Off(0u), fields64["pOffset"])
        assertEquals(Elf64Addr(0x400000u), fields64["pVaddr"])
        assertEquals(Elf64Addr(0x400000u), fields64["pPaddr"])
        assertEquals(Elf64Xword(0x1000UL), fields64["pFilesz"])
        assertEquals(Elf64Xword(0x1000UL), fields64["pMemsz"])
        assertEquals(Elf64Xword(0x1000UL), fields64["pAlign"])
    }
} 