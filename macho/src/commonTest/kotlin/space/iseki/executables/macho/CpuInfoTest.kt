package space.iseki.executables.macho

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for the CpuInfo class
 */
class CpuInfoTest {
    companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }

    @Test
    fun testCpuInfoCreation() {
        // Test creating CpuInfo from CPU type and subtype
        val cpuInfo = CpuInfo.valueOf(CpuType.X86, X86CpuSubtype.ALL.value.toUInt())

        // Verify the CPU type and subtype
        assertEquals(CpuType.X86, cpuInfo.cpuType)
        assertEquals(X86CpuSubtype.ALL.value.toUInt(), cpuInfo.cpuSubtype)

        // Verify it's not a 64-bit CPU
        assertFalse(cpuInfo.is64Bit)

        // Verify the base CPU type
        assertEquals(CpuType.X86, cpuInfo.baseCpuType)
    }

    @Test
    fun testCpuInfo64Bit() {
        // Test creating a 64-bit CPU info
        val cpuInfo = CpuInfo.valueOf(CpuType.X86_64, X86CpuSubtype.ALL.value.toUInt())

        // Verify it's a 64-bit CPU
        assertTrue(cpuInfo.is64Bit)

        // Verify the base CPU type (without 64-bit flag)
        assertEquals(CpuType.X86, cpuInfo.baseCpuType)
    }

    @Test
    fun testCpuInfoToString() {
        // Test the toString method for X86 CPU
        val x86CpuInfo = CpuInfo.valueOf(CpuType.X86, X86CpuSubtype.PENTIUM_3.value.toUInt())
        assertEquals("CpuInfo(type=X86, subtype=PENTIUM_3)", x86CpuInfo.toString())

        // Test the toString method for ARM CPU
        val armCpuInfo = CpuInfo.valueOf(CpuType.ARM, ArmCpuSubtype.V7.value.toUInt())
        assertEquals("CpuInfo(type=ARM, subtype=V7)", armCpuInfo.toString())

        // Test the toString method for an unknown CPU type
        val unknownCpuInfo = CpuInfo.valueOf(CpuType.VAX, 0x123u)
        assertEquals("CpuInfo(type=VAX, subtype=0x00000123)", unknownCpuInfo.toString())
    }

    @Test
    fun testCpuInfoSerialization() {
        // Create a CpuInfo instance for X86
        val original = CpuInfo.valueOf(CpuType.X86, X86CpuSubtype.PENTIUM_4.value.toUInt())

        // Serialize to JSON
        val serialized = json.encodeToString(CpuInfo.serializer(), original)
        println("X86 CpuInfo serialization result: $serialized")

        // Deserialize from JSON
        val deserialized = json.decodeFromString(CpuInfo.serializer(), serialized)

        // Verify the deserialized object equals the original
        assertEquals(original, deserialized)
        assertEquals(original.cpuType, deserialized.cpuType)
        assertEquals(original.cpuSubtype, deserialized.cpuSubtype)
    }

    @Test
    fun testArmCpuInfoSerialization() {
        // Create a CpuInfo instance for ARM
        val original = CpuInfo.valueOf(CpuType.ARM, ArmCpuSubtype.V8.value.toUInt())

        // Serialize to JSON
        val serialized = json.encodeToString(CpuInfo.serializer(), original)
        println("ARM CpuInfo serialization result: $serialized")

        // Deserialize from JSON
        val deserialized = json.decodeFromString(CpuInfo.serializer(), serialized)

        // Verify the deserialized object equals the original
        assertEquals(original, deserialized)
        assertEquals(original.cpuType, deserialized.cpuType)
        assertEquals(original.cpuSubtype, deserialized.cpuSubtype)
    }

    @Test
    fun test64BitCpuInfoSerialization() {
        // Create a 64-bit CpuInfo instance
        val original = CpuInfo.valueOf(CpuType.ARM_64, ArmCpuSubtype.V8.value.toUInt())

        // Serialize to JSON
        val serialized = json.encodeToString(CpuInfo.serializer(), original)
        println("64-bit ARM CpuInfo serialization result: $serialized")

        // 验证序列化的JSON包含正确的类型和子类型
        assertTrue(serialized.contains("\"type\""))
        assertTrue(serialized.contains("\"ARM_64\""))
        assertTrue(serialized.contains("\"subtype\""))
        assertTrue(serialized.contains("\"V8\""))

        // 反序列化并验证
        val deserialized = json.decodeFromString(CpuInfo.serializer(), serialized)
        assertEquals(original, deserialized)
        assertEquals(original.cpuType, deserialized.cpuType)
        assertEquals(original.cpuSubtype, deserialized.cpuSubtype)
    }

    @Test
    fun testUnknownCpuSubtypeSerialization() {
        // Create a CpuInfo with an unknown subtype
        val original = CpuInfo.valueOf(CpuType.POWERPC, 0xABCDu)

        // Serialize to JSON
        val serialized = json.encodeToString(CpuInfo.serializer(), original)
        println("Unknown subtype CpuInfo serialization result: $serialized")

        // 验证序列化的JSON包含正确的类型和子类型
        assertTrue(serialized.contains("\"type\""))
        assertTrue(serialized.contains("\"POWERPC\""))
        assertTrue(serialized.contains("\"subtype\""))
        assertTrue(serialized.contains("\"0x0000abcd\""))

        // 反序列化并验证
        val deserialized = json.decodeFromString(CpuInfo.serializer(), serialized)
        assertEquals(original, deserialized)
        assertEquals(original.cpuType, deserialized.cpuType)
        assertEquals(original.cpuSubtype, deserialized.cpuSubtype)
    }
} 