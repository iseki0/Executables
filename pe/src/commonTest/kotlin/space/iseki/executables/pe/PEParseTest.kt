package space.iseki.executables.pe

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 测试PE相关类的parse函数
 */
class PEParseTest {

    @Test
    fun testCoffHeaderParse() {
        // 创建一个模拟的COFF头部字节数组
        val coffBytes = ByteArray(CoffHeader.LENGTH)

        // 设置machine (0x014c = IMAGE_FILE_MACHINE_I386)
        coffBytes[0] = 0x4c.toByte()
        coffBytes[1] = 0x01.toByte()

        // 设置numbersOfSections (3)
        coffBytes[2] = 0x03.toByte()
        coffBytes[3] = 0x00.toByte()

        // 设置timeDateStamp (0x5F3E2D1C)
        coffBytes[4] = 0x1c.toByte()
        coffBytes[5] = 0x2d.toByte()
        coffBytes[6] = 0x3e.toByte()
        coffBytes[7] = 0x5f.toByte()

        // 设置pointerToSymbolTable (0x00000000)
        coffBytes[8] = 0x00.toByte()
        coffBytes[9] = 0x00.toByte()
        coffBytes[10] = 0x00.toByte()
        coffBytes[11] = 0x00.toByte()

        // 设置numbersOfSymbols (0)
        coffBytes[12] = 0x00.toByte()
        coffBytes[13] = 0x00.toByte()
        coffBytes[14] = 0x00.toByte()
        coffBytes[15] = 0x00.toByte()

        // 设置sizeOfOptionalHeader (0x00E0)
        coffBytes[16] = 0xe0.toByte()
        coffBytes[17] = 0x00.toByte()

        // 设置characteristics (0x010F)
        coffBytes[18] = 0x0f.toByte()
        coffBytes[19] = 0x01.toByte()

        // 解析COFF头部
        val coffHeader = CoffHeader.parse(coffBytes, 0)

        // 验证解析结果
        assertEquals(3.toUShort(), coffHeader.numbersOfSections)
        assertEquals(0x5F3E2D1Cu, coffHeader.timeDateStamp.value)
        assertEquals(0u, coffHeader.pointerToSymbolTable.value)
        assertEquals(0u, coffHeader.numbersOfSymbols)
        assertEquals(0xe0.toUShort(), coffHeader.sizeOfOptionalHeader)
    }

    @Test
    fun testStandardHeaderParse() {
        // 创建一个模拟的标准头部字节数组
        val stdBytes = ByteArray(StandardHeader.MAX_LENGTH)

        // 设置magic (0x010B = PE32)
        stdBytes[0] = 0x0b.toByte()
        stdBytes[1] = 0x01.toByte()

        // 设置majorLinkerVersion (6)
        stdBytes[2] = 0x06.toByte()

        // 设置minorLinkerVersion (0)
        stdBytes[3] = 0x00.toByte()

        // 设置sizeOfCode (0x00001000)
        stdBytes[4] = 0x00.toByte()
        stdBytes[5] = 0x10.toByte()
        stdBytes[6] = 0x00.toByte()
        stdBytes[7] = 0x00.toByte()

        // 设置sizeOfInitializedData (0x00002000)
        stdBytes[8] = 0x00.toByte()
        stdBytes[9] = 0x20.toByte()
        stdBytes[10] = 0x00.toByte()
        stdBytes[11] = 0x00.toByte()

        // 设置sizeOfUninitializedData (0x00000000)
        stdBytes[12] = 0x00.toByte()
        stdBytes[13] = 0x00.toByte()
        stdBytes[14] = 0x00.toByte()
        stdBytes[15] = 0x00.toByte()

        // 设置addressOfEntryPoint (0x00001000)
        stdBytes[16] = 0x00.toByte()
        stdBytes[17] = 0x10.toByte()
        stdBytes[18] = 0x00.toByte()
        stdBytes[19] = 0x00.toByte()

        // 设置baseOfCode (0x00001000)
        stdBytes[20] = 0x00.toByte()
        stdBytes[21] = 0x10.toByte()
        stdBytes[22] = 0x00.toByte()
        stdBytes[23] = 0x00.toByte()

        // 设置baseOfData (0x00002000) - 仅PE32格式有此字段
        stdBytes[24] = 0x00.toByte()
        stdBytes[25] = 0x20.toByte()
        stdBytes[26] = 0x00.toByte()
        stdBytes[27] = 0x00.toByte()

        // 解析标准头部
        val stdHeader = StandardHeader.parse(stdBytes, 0)

        // 验证解析结果
        assertEquals(PE32Magic.PE32, stdHeader.magic)
        assertEquals(6, stdHeader.majorLinkerVersion.toInt())
        assertEquals(0, stdHeader.minorLinkerVersion.toInt())
        assertEquals(0x1000u, stdHeader.sizeOfCode)
        assertEquals(0x2000u, stdHeader.sizeOfInitializedData)
        assertEquals(0u, stdHeader.sizeOfUninitializedData)
        assertEquals(0x1000u, stdHeader.addressOfEntryPoint.value)
        assertEquals(0x1000u, stdHeader.baseOfCode.value)
        assertEquals(0x2000u, stdHeader.baseOfData.value)
    }

    @Test
    fun testDataDirectoryItemParse() {
        // 创建一个模拟的数据目录项字节数组
        val ddBytes = ByteArray(8)

        // 设置virtualAddress (0x00001000)
        ddBytes[0] = 0x00.toByte()
        ddBytes[1] = 0x10.toByte()
        ddBytes[2] = 0x00.toByte()
        ddBytes[3] = 0x00.toByte()

        // 设置size (0x00000200)
        ddBytes[4] = 0x00.toByte()
        ddBytes[5] = 0x02.toByte()
        ddBytes[6] = 0x00.toByte()
        ddBytes[7] = 0x00.toByte()

        // 解析数据目录项
        val ddItem = DataDirectoryItem.parse(ddBytes, 0)

        // 验证解析结果
        assertEquals(0x1000u, ddItem.virtualAddress.value)
        assertEquals(0x200, ddItem.size)
    }

    @Test
    fun testSectionTableItemParse() {
        // 创建一个模拟的节表项字节数组
        val sectionBytes = ByteArray(SectionTableItem.LENGTH)

        // 设置name (".text")
        // 使用跨平台兼容的方式设置字节数组
        val nameBytes = byteArrayOf(
            '.'.code.toByte(),
            't'.code.toByte(),
            'e'.code.toByte(),
            'x'.code.toByte(),
            't'.code.toByte(),
            0, 0, 0
        )
        for (i in nameBytes.indices) {
            sectionBytes[i] = nameBytes[i]
        }

        // 设置virtualSize (0x00001000)
        sectionBytes[8] = 0x00.toByte()
        sectionBytes[9] = 0x10.toByte()
        sectionBytes[10] = 0x00.toByte()
        sectionBytes[11] = 0x00.toByte()

        // 设置virtualAddress (0x00001000)
        sectionBytes[12] = 0x00.toByte()
        sectionBytes[13] = 0x10.toByte()
        sectionBytes[14] = 0x00.toByte()
        sectionBytes[15] = 0x00.toByte()

        // 设置sizeOfRawData (0x00000800)
        sectionBytes[16] = 0x00.toByte()
        sectionBytes[17] = 0x08.toByte()
        sectionBytes[18] = 0x00.toByte()
        sectionBytes[19] = 0x00.toByte()

        // 设置pointerToRawData (0x00000400)
        sectionBytes[20] = 0x00.toByte()
        sectionBytes[21] = 0x04.toByte()
        sectionBytes[22] = 0x00.toByte()
        sectionBytes[23] = 0x00.toByte()

        // 设置pointerToRelocations (0x00000000)
        sectionBytes[24] = 0x00.toByte()
        sectionBytes[25] = 0x00.toByte()
        sectionBytes[26] = 0x00.toByte()
        sectionBytes[27] = 0x00.toByte()

        // 设置pointerToLinenumbers (0x00000000)
        sectionBytes[28] = 0x00.toByte()
        sectionBytes[29] = 0x00.toByte()
        sectionBytes[30] = 0x00.toByte()
        sectionBytes[31] = 0x00.toByte()

        // 设置numberOfRelocations (0)
        sectionBytes[32] = 0x00.toByte()
        sectionBytes[33] = 0x00.toByte()

        // 设置numberOfLinenumbers (0)
        sectionBytes[34] = 0x00.toByte()
        sectionBytes[35] = 0x00.toByte()

        // 设置characteristics (0x60000020) - 包含代码，可执行，可读
        sectionBytes[36] = 0x20.toByte()
        sectionBytes[37] = 0x00.toByte()
        sectionBytes[38] = 0x00.toByte()
        sectionBytes[39] = 0x60.toByte()

        // 解析节表项
        val sectionItem = SectionTableItem.parse(sectionBytes, 0)

        // 验证解析结果
        assertEquals(".text", sectionItem.name)
        assertEquals(0x1000u, sectionItem.virtualSize)
        assertEquals(0x1000u, sectionItem.virtualAddress.value)
        assertEquals(0x800u, sectionItem.sizeOfRawData)
        assertEquals(0x400u, sectionItem.pointerToRawData.value)
        assertEquals(0u, sectionItem.pointerToRelocations.value)
        assertEquals(0u, sectionItem.pointerToLinenumbers.value)
        assertEquals(0.toUShort(), sectionItem.numberOfRelocations)
        assertEquals(0.toUShort(), sectionItem.numberOfLinenumbers)
    }
} 