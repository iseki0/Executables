package space.iseki.executables.elf

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * 测试ElfShdr、Elf32Shdr和Elf64Shdr类的序列化和反序列化功能
 */
class ElfShdrTest {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun testElf32ShdrSerialization() {
        // 创建一个Elf32Shdr实例
        val original = Elf32Shdr(
            shName = Elf32Word(1u),
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Elf32Addr(0x8048000u),
            shOffset = Elf32Off(0x1000u),
            shSize = Elf32Word(0x2000u),
            shLink = Elf32Word(0u),
            shInfo = Elf32Word(0u),
            shAddralign = Elf32Word(4u),
            shEntsize = Elf32Word(0u)
        )

        // 序列化为JSON
        val serialized = json.encodeToString<ElfShdr>(original)
        println("Elf32Shdr序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfShdr>(serialized)

        // 验证反序列化的对象是Elf32Shdr类型
        assertIs<Elf32Shdr>(deserialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(Elf32Word(1u), deserialized.shName)
        assertEquals(ElfSType.SHT_PROGBITS, deserialized.shType)
        assertEquals(ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR, deserialized.shFlags)
        assertEquals(Elf32Addr(0x8048000u), deserialized.shAddr)
        assertEquals(Elf32Off(0x1000u), deserialized.shOffset)
        assertEquals(Elf32Word(0x2000u), deserialized.shSize)
        assertEquals(Elf32Word(0u), deserialized.shLink)
        assertEquals(Elf32Word(0u), deserialized.shInfo)
        assertEquals(Elf32Word(4u), deserialized.shAddralign)
        assertEquals(Elf32Word(0u), deserialized.shEntsize)
    }

    @Test
    fun testElf64ShdrSerialization() {
        // 创建一个Elf64Shdr实例
        val original = Elf64Shdr(
            shName = Elf64Word(1u),
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Elf64Addr(0x400000u),
            shOffset = Elf64Off(0x1000u),
            shSize = Elf64Xword(0x2000UL),
            shLink = Elf64Word(0u),
            shInfo = Elf64Word(0u),
            shAddralign = Elf64Xword(8UL),
            shEntsize = Elf64Xword(0UL)
        )

        // 序列化为JSON
        val serialized = json.encodeToString<ElfShdr>(original)
        println("Elf64Shdr序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfShdr>(serialized)

        // 验证反序列化的对象是Elf64Shdr类型
        assertIs<Elf64Shdr>(deserialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(Elf64Word(1u), deserialized.shName)
        assertEquals(ElfSType.SHT_PROGBITS, deserialized.shType)
        assertEquals(ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR, deserialized.shFlags)
        assertEquals(Elf64Addr(0x400000u), deserialized.shAddr)
        assertEquals(Elf64Off(0x1000u), deserialized.shOffset)
        assertEquals(Elf64Xword(0x2000UL), deserialized.shSize)
        assertEquals(Elf64Word(0u), deserialized.shLink)
        assertEquals(Elf64Word(0u), deserialized.shInfo)
        assertEquals(Elf64Xword(8UL), deserialized.shAddralign)
        assertEquals(Elf64Xword(0UL), deserialized.shEntsize)
    }

    @Test
    fun testElfShdrPolymorphicSerialization() {
        // 创建两个不同类型的ElfShdr实例
        val elf32Shdr: ElfShdr = Elf32Shdr(
            shName = Elf32Word(1u),
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Elf32Addr(0x8048000u),
            shOffset = Elf32Off(0x1000u),
            shSize = Elf32Word(0x2000u),
            shLink = Elf32Word(0u),
            shInfo = Elf32Word(0u),
            shAddralign = Elf32Word(4u),
            shEntsize = Elf32Word(0u)
        )

        val elf64Shdr: ElfShdr = Elf64Shdr(
            shName = Elf64Word(1u),
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Elf64Addr(0x400000u),
            shOffset = Elf64Off(0x1000u),
            shSize = Elf64Xword(0x2000UL),
            shLink = Elf64Word(0u),
            shInfo = Elf64Word(0u),
            shAddralign = Elf64Xword(8UL),
            shEntsize = Elf64Xword(0UL)
        )

        // 序列化为JSON
        val serialized32 = json.encodeToString<ElfShdr>(elf32Shdr)
        val serialized64 = json.encodeToString<ElfShdr>(elf64Shdr)

        println("Elf32Shdr多态序列化结果: $serialized32")
        println("Elf64Shdr多态序列化结果: $serialized64")

        // 从JSON反序列化
        val deserialized32 = json.decodeFromString<ElfShdr>(serialized32)
        val deserialized64 = json.decodeFromString<ElfShdr>(serialized64)

        // 验证反序列化的对象类型
        assertIs<Elf32Shdr>(deserialized32)
        assertIs<Elf64Shdr>(deserialized64)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(elf32Shdr, deserialized32)
        assertEquals(elf64Shdr, deserialized64)
    }

    @Test
    fun testElfShdrFields() {
        // 创建Elf32Shdr和Elf64Shdr实例
        val elf32Shdr = Elf32Shdr(
            shName = Elf32Word(1u),
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Elf32Addr(0x8048000u),
            shOffset = Elf32Off(0x1000u),
            shSize = Elf32Word(0x2000u),
            shLink = Elf32Word(0u),
            shInfo = Elf32Word(0u),
            shAddralign = Elf32Word(4u),
            shEntsize = Elf32Word(0u)
        )

        val elf64Shdr = Elf64Shdr(
            shName = Elf64Word(1u),
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Elf64Addr(0x400000u),
            shOffset = Elf64Off(0x1000u),
            shSize = Elf64Xword(0x2000UL),
            shLink = Elf64Word(0u),
            shInfo = Elf64Word(0u),
            shAddralign = Elf64Xword(8UL),
            shEntsize = Elf64Xword(0UL)
        )

        // 验证Elf32Shdr的fields映射
        val fields32 = elf32Shdr.fields
        assertEquals(10, fields32.size)
        assertEquals(Elf32Word(1u), fields32["shName"])
        assertEquals(ElfSType.SHT_PROGBITS, fields32["shType"])
        assertEquals(ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR, fields32["shFlags"])
        assertEquals(Elf32Addr(0x8048000u), fields32["shAddr"])
        assertEquals(Elf32Off(0x1000u), fields32["shOffset"])
        assertEquals(Elf32Word(0x2000u), fields32["shSize"])
        assertEquals(Elf32Word(0u), fields32["shLink"])
        assertEquals(Elf32Word(0u), fields32["shInfo"])
        assertEquals(Elf32Word(4u), fields32["shAddralign"])
        assertEquals(Elf32Word(0u), fields32["shEntsize"])

        // 验证Elf64Shdr的fields映射
        val fields64 = elf64Shdr.fields
        assertEquals(10, fields64.size)
        assertEquals(Elf64Word(1u), fields64["shName"])
        assertEquals(ElfSType.SHT_PROGBITS, fields64["shType"])
        assertEquals(ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR, fields64["shFlags"])
        assertEquals(Elf64Addr(0x400000u), fields64["shAddr"])
        assertEquals(Elf64Off(0x1000u), fields64["shOffset"])
        assertEquals(Elf64Xword(0x2000UL), fields64["shSize"])
        assertEquals(Elf64Word(0u), fields64["shLink"])
        assertEquals(Elf64Word(0u), fields64["shInfo"])
        assertEquals(Elf64Xword(8UL), fields64["shAddralign"])
        assertEquals(Elf64Xword(0UL), fields64["shEntsize"])
    }

    @Test
    fun testElfShdrWithDifferentTypes() {
        // 测试不同的节类型
        val sectionTypes = listOf(
            ElfSType.SHT_NULL,
            ElfSType.SHT_PROGBITS,
            ElfSType.SHT_SYMTAB,
            ElfSType.SHT_STRTAB,
            ElfSType.SHT_RELA,
            ElfSType.SHT_HASH,
            ElfSType.SHT_DYNAMIC
        )

        for (sectionType in sectionTypes) {
            val original = Elf32Shdr(
                shName = Elf32Word(1u),
                shType = sectionType,
                shFlags = ElfSFlags.SHF_ALLOC,
                shAddr = Elf32Addr(0x8048000u),
                shOffset = Elf32Off(0x1000u),
                shSize = Elf32Word(0x2000u),
                shLink = Elf32Word(0u),
                shInfo = Elf32Word(0u),
                shAddralign = Elf32Word(4u),
                shEntsize = Elf32Word(0u)
            )

            val serialized = json.encodeToString<ElfShdr>(original)
            val deserialized = json.decodeFromString<ElfShdr>(serialized)

            assertEquals(original, deserialized)
            assertEquals(sectionType, deserialized.shType)
        }
    }

    @Test
    fun testElfShdrWithDifferentFlags() {
        // 测试不同的节标志组合
        val flagCombinations = listOf(
            ElfSFlags.SHF_WRITE,
            ElfSFlags.SHF_ALLOC,
            ElfSFlags.SHF_EXECINSTR,
            ElfSFlags.SHF_WRITE or ElfSFlags.SHF_ALLOC,
            ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            ElfSFlags.SHF_WRITE or ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR
        )

        for (flags in flagCombinations) {
            val original = Elf32Shdr(
                shName = Elf32Word(1u),
                shType = ElfSType.SHT_PROGBITS,
                shFlags = flags,
                shAddr = Elf32Addr(0x8048000u),
                shOffset = Elf32Off(0x1000u),
                shSize = Elf32Word(0x2000u),
                shLink = Elf32Word(0u),
                shInfo = Elf32Word(0u),
                shAddralign = Elf32Word(4u),
                shEntsize = Elf32Word(0u)
            )

            val serialized = json.encodeToString<ElfShdr>(original)
            val deserialized = json.decodeFromString<ElfShdr>(serialized)

            assertEquals(original, deserialized)
            assertEquals(flags, deserialized.shFlags)
        }
    }
} 