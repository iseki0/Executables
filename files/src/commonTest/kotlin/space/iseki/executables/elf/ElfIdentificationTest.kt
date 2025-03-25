package space.iseki.executables.elf

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 测试ElfIdentification类的序列化和反序列化功能
 */
class ElfIdentificationTest {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun testElfIdentificationSerialization() {
        // 创建一个ElfIdentification实例
        val original = ElfIdentification(
            eiClass = ElfClass.ELFCLASS32,
            eiData = ElfData.ELFDATA2LSB,
            eiVersion = 1u.toUByte(),
            eiOsAbi = ElfOsAbi.ELFOSABI_LINUX,
            eiAbiVersion = 0u.toUByte()
        )

        // 序列化为JSON
        val serialized = json.encodeToString<ElfIdentification>(original)
        println("ElfIdentification序列化结果: $serialized")

        // 从JSON反序列化
        val deserialized = json.decodeFromString<ElfIdentification>(serialized)

        // 验证反序列化后的对象与原始对象相等
        assertEquals(original, deserialized)

        // 验证各个字段的值
        assertEquals(ElfClass.ELFCLASS32, deserialized.eiClass)
        assertEquals(ElfData.ELFDATA2LSB, deserialized.eiData)
        assertEquals(1u.toUByte(), deserialized.eiVersion)
        assertEquals(ElfOsAbi.ELFOSABI_LINUX, deserialized.eiOsAbi)
        assertEquals(0u.toUByte(), deserialized.eiAbiVersion)
    }

    @Test
    fun testElfIdentificationFields() {
        // 创建一个ElfIdentification实例
        val ident = ElfIdentification(
            eiClass = ElfClass.ELFCLASS64,
            eiData = ElfData.ELFDATA2MSB,
            eiVersion = 1u.toUByte(),
            eiOsAbi = ElfOsAbi.ELFOSABI_FREEBSD,
            eiAbiVersion = 1u.toUByte()
        )

        // 验证fields映射
        val fields = ident.fields

        // 打印实际的字段内容，帮助调试
        println("实际的fields内容: $fields")
        println("fields.keys: ${fields.keys}")
        println("fields.values: ${fields.values}")

        // 逐个检查字段是否存在
        assertTrue(fields.containsKey("eiClass"), "fields应该包含eiClass键")
        assertTrue(fields.containsKey("eiData"), "fields应该包含eiData键")
        assertTrue(fields.containsKey("eiVersion"), "fields应该包含eiVersion键")
        assertTrue(fields.containsKey("eiOsAbi"), "fields应该包含eiOsAbi键")
        assertTrue(fields.containsKey("eiAbiVersion"), "fields应该包含eiAbiVersion键")

        // 逐个验证字段值
        assertEquals(ElfClass.ELFCLASS64, fields["eiClass"], "eiClass字段值不匹配")
        assertEquals(ElfData.ELFDATA2MSB, fields["eiData"], "eiData字段值不匹配")
        assertEquals(1u.toUByte(), fields["eiVersion"], "eiVersion字段值不匹配")
        assertEquals(ElfOsAbi.ELFOSABI_FREEBSD, fields["eiOsAbi"], "eiOsAbi字段值不匹配")
        assertEquals(1u.toUByte(), fields["eiAbiVersion"], "eiAbiVersion字段值不匹配")
    }

    @Test
    fun testElfIdentificationWithDifferentOsAbi() {
        // 测试不同的操作系统ABI
        val osAbis = listOf(
            ElfOsAbi.ELFOSABI_NONE,
            ElfOsAbi.ELFOSABI_LINUX,
            ElfOsAbi.ELFOSABI_FREEBSD,
            ElfOsAbi.ELFOSABI_SOLARIS
        )

        for (osAbi in osAbis) {
            val original = ElfIdentification(
                eiClass = ElfClass.ELFCLASS32,
                eiData = ElfData.ELFDATA2LSB,
                eiVersion = 1u.toUByte(),
                eiOsAbi = osAbi,
                eiAbiVersion = 0u.toUByte()
            )

            val serialized = json.encodeToString<ElfIdentification>(original)
            val deserialized = json.decodeFromString<ElfIdentification>(serialized)

            assertEquals(original, deserialized)
            assertEquals(osAbi, deserialized.eiOsAbi)
        }
    }

    @Test
    fun testElfIdentificationWithDifferentClasses() {
        // 测试不同的ELF类别
        val classes = listOf(
            ElfClass.ELFCLASS32,
            ElfClass.ELFCLASS64
        )

        for (elfClass in classes) {
            val original = ElfIdentification(
                eiClass = elfClass,
                eiData = ElfData.ELFDATA2LSB,
                eiVersion = 1u.toUByte(),
                eiOsAbi = ElfOsAbi.ELFOSABI_NONE,
                eiAbiVersion = 0u.toUByte()
            )

            val serialized = json.encodeToString<ElfIdentification>(original)
            val deserialized = json.decodeFromString<ElfIdentification>(serialized)

            assertEquals(original, deserialized)
            assertEquals(elfClass, deserialized.eiClass)
        }
    }

    @Test
    fun testElfIdentificationWithDifferentDataEncodings() {
        // 测试不同的数据编码
        val dataEncodings = listOf(
            ElfData.ELFDATA2LSB,
            ElfData.ELFDATA2MSB
        )

        for (dataEncoding in dataEncodings) {
            val original = ElfIdentification(
                eiClass = ElfClass.ELFCLASS32,
                eiData = dataEncoding,
                eiVersion = 1u.toUByte(),
                eiOsAbi = ElfOsAbi.ELFOSABI_NONE,
                eiAbiVersion = 0u.toUByte()
            )

            val serialized = json.encodeToString<ElfIdentification>(original)
            val deserialized = json.decodeFromString<ElfIdentification>(serialized)

            assertEquals(original, deserialized)
            assertEquals(dataEncoding, deserialized.eiData)
        }
    }
} 