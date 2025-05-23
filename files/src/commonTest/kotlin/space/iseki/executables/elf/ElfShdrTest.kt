package space.iseki.executables.elf

import kotlinx.serialization.json.Json
import space.iseki.executables.common.Address32
import space.iseki.executables.common.Address64
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 测试统一的ElfShdr类的序列化和反序列化功能
 */
class ElfShdrTest {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun testElf32ShdrSerialization() {
        // 创建一个32位ElfShdr实例
        val original = ElfShdr(
            is64Bit = false,
            shName = 1u,
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Address64(0x8048000uL),
            shOffset = 0x1000uL,
            shSize = 0x2000uL,
            shLink = 0u,
            shInfo = 0u,
            shAddralign = 4uL,
            shEntsize = 0uL
        )

        // 序列化为JSON
        val serialized = json.encodeToString(original)
        println("Elf32Shdr序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfShdr>(serialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(false, deserialized.is64Bit)
        assertEquals(1u, deserialized.shName)
        assertEquals(ElfSType.SHT_PROGBITS, deserialized.shType)
        assertEquals(ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR, deserialized.shFlags)
        assertEquals(Address64(0x8048000uL), deserialized.shAddr)
        assertEquals(0x1000uL, deserialized.shOffset)
        assertEquals(0x2000uL, deserialized.shSize)
        assertEquals(0u, deserialized.shLink)
        assertEquals(0u, deserialized.shInfo)
        assertEquals(4uL, deserialized.shAddralign)
        assertEquals(0uL, deserialized.shEntsize)
    }

    @Test
    fun testElf64ShdrSerialization() {
        // 创建一个64位ElfShdr实例
        val original = ElfShdr(
            is64Bit = true,
            shName = 1u,
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Address64(0x400000uL),
            shOffset = 0x1000uL,
            shSize = 0x2000uL,
            shLink = 0u,
            shInfo = 0u,
            shAddralign = 8uL,
            shEntsize = 0uL
        )

        // 序列化为JSON
        val serialized = json.encodeToString(original)
        println("Elf64Shdr序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfShdr>(serialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(true, deserialized.is64Bit)
        assertEquals(1u, deserialized.shName)
        assertEquals(ElfSType.SHT_PROGBITS, deserialized.shType)
        assertEquals(ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR, deserialized.shFlags)
        assertEquals(Address64(0x400000uL), deserialized.shAddr)
        assertEquals(0x1000uL, deserialized.shOffset)
        assertEquals(0x2000uL, deserialized.shSize)
        assertEquals(0u, deserialized.shLink)
        assertEquals(0u, deserialized.shInfo)
        assertEquals(8uL, deserialized.shAddralign)
        assertEquals(0uL, deserialized.shEntsize)
    }

    @Test
    fun testElfShdrPolymorphicSerialization() {
        // 创建两个不同类型的ElfShdr实例
        val elf32Shdr = ElfShdr(
            is64Bit = false,
            shName = 1u,
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Address64(0x8048000uL),
            shOffset = 0x1000uL,
            shSize = 0x2000uL,
            shLink = 0u,
            shInfo = 0u,
            shAddralign = 4uL,
            shEntsize = 0uL
        )

        val elf64Shdr = ElfShdr(
            is64Bit = true,
            shName = 1u,
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Address64(0x400000uL),
            shOffset = 0x1000uL,
            shSize = 0x2000uL,
            shLink = 0u,
            shInfo = 0u,
            shAddralign = 8uL,
            shEntsize = 0uL
        )

        // 序列化为JSON
        val serialized32 = json.encodeToString(elf32Shdr)
        val serialized64 = json.encodeToString(elf64Shdr)

        println("Elf32Shdr多态序列化结果: $serialized32")
        println("Elf64Shdr多态序列化结果: $serialized64")

        // 从JSON反序列化
        val deserialized32 = json.decodeFromString<ElfShdr>(serialized32)
        val deserialized64 = json.decodeFromString<ElfShdr>(serialized64)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(elf32Shdr, deserialized32)
        assertEquals(elf64Shdr, deserialized64)
    }

    @Test
    fun testElfShdrFields() {
        // 创建32位和64位ElfShdr实例
        val elf32Shdr = ElfShdr(
            is64Bit = false,
            shName = 1u,
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Address64(0x8048000uL),
            shOffset = 0x1000uL,
            shSize = 0x2000uL,
            shLink = 0u,
            shInfo = 0u,
            shAddralign = 4uL,
            shEntsize = 0uL
        )

        val elf64Shdr = ElfShdr(
            is64Bit = true,
            shName = 1u,
            shType = ElfSType.SHT_PROGBITS,
            shFlags = ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR,
            shAddr = Address64(0x400000uL),
            shOffset = 0x1000uL,
            shSize = 0x2000uL,
            shLink = 0u,
            shInfo = 0u,
            shAddralign = 8uL,
            shEntsize = 0uL
        )

        // 验证32位ElfShdr的fields映射
        val fields32 = elf32Shdr.fields
        assertEquals(12, fields32.size) // 包含is64Bit字段
        assertEquals(false, fields32["is64Bit"])
        assertEquals(1u, fields32["shName"])
        assertEquals(ElfSType.SHT_PROGBITS, fields32["shType"])
        assertEquals(ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR, fields32["shFlags"])
        assertEquals(Address32(0x8048000u), fields32["shAddr"])
        assertEquals(0x1000u, fields32["shOffset"])
        assertEquals(0x2000u, fields32["shSize"])
        assertEquals(0u, fields32["shLink"])
        assertEquals(0u, fields32["shInfo"])
        assertEquals(4u, fields32["shAddralign"])
        assertEquals(0u, fields32["shEntsize"])
        assertEquals("", fields32["name"])

        // 验证64位ElfShdr的fields映射
        val fields64 = elf64Shdr.fields
        assertEquals(12, fields64.size) // 包含is64Bit字段
        assertEquals(true, fields64["is64Bit"])
        assertEquals(1u, fields64["shName"])
        assertEquals(ElfSType.SHT_PROGBITS, fields64["shType"])
        assertEquals(ElfSFlags.SHF_ALLOC or ElfSFlags.SHF_EXECINSTR, fields64["shFlags"])
        assertEquals(Address64(0x400000uL), fields64["shAddr"])
        assertEquals(0x1000uL, fields64["shOffset"])
        assertEquals(0x2000uL, fields64["shSize"])
        assertEquals(0u, fields64["shLink"])
        assertEquals(0u, fields64["shInfo"])
        assertEquals(8uL, fields64["shAddralign"])
        assertEquals(0uL, fields64["shEntsize"])
        assertEquals("", fields64["name"])
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
            val original = ElfShdr(
                is64Bit = false,
                shName = 1u,
                shType = sectionType,
                shFlags = ElfSFlags.SHF_ALLOC,
                shAddr = Address64(0x8048000uL),
                shOffset = 0x1000uL,
                shSize = 0x2000uL,
                shLink = 0u,
                shInfo = 0u,
                shAddralign = 4uL,
                shEntsize = 0uL
            )

            val serialized = json.encodeToString(original)
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
            val original = ElfShdr(
                is64Bit = false,
                shName = 1u,
                shType = ElfSType.SHT_PROGBITS,
                shFlags = flags,
                shAddr = Address64(0x8048000uL),
                shOffset = 0x1000uL,
                shSize = 0x2000uL,
                shLink = 0u,
                shInfo = 0u,
                shAddralign = 4uL,
                shEntsize = 0uL
            )

            val serialized = json.encodeToString(original)
            val deserialized = json.decodeFromString<ElfShdr>(serialized)

            assertEquals(original, deserialized)
            assertEquals(flags, deserialized.shFlags)
        }
    }
} 