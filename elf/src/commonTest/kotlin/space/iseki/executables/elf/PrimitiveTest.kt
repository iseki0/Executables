package space.iseki.executables.elf

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimitiveTest {

    @Test
    fun testPrimitiveComparison() {
        // 测试相同类型的比较
        val half1 = Elf32Half(10u)
        val half2 = Elf32Half(20u)
        assertTrue(half1 < half2)
        assertTrue(half2 > half1)
        assertTrue(half1 == half1)

        val word1 = Elf32Word(100u)
        val word2 = Elf32Word(200u)
        assertTrue(word1 < word2)
        assertTrue(word2 > word1)
        assertTrue(word1 == word1)

        val sword1 = Elf32Sword(-10)
        val sword2 = Elf32Sword(10)
        assertTrue(sword1 < sword2)
        assertTrue(sword2 > sword1)
        assertTrue(sword1 == sword1)

        val addr1 = Elf32Addr(0x1000u)
        val addr2 = Elf32Addr(0x2000u)
        assertTrue(addr1 < addr2)
        assertTrue(addr2 > addr1)
        assertTrue(addr1 == addr1)

        val off1 = Elf32Off(0x100u)
        val off2 = Elf32Off(0x200u)
        assertTrue(off1 < off2)
        assertTrue(off2 > off1)
        assertTrue(off1 == off1)

        // 测试 Elf64 类型
        val half64_1 = Elf64Half(10u)
        val half64_2 = Elf64Half(20u)
        assertTrue(half64_1 < half64_2)

        val word64_1 = Elf64Word(100u)
        val word64_2 = Elf64Word(200u)
        assertTrue(word64_1 < word64_2)

        val xword64_1 = Elf64Xword(1000UL)
        val xword64_2 = Elf64Xword(2000UL)
        assertTrue(xword64_1 < xword64_2)

        val sxword64_1 = Elf64Sxword(-1000L)
        val sxword64_2 = Elf64Sxword(1000L)
        assertTrue(sxword64_1 < sxword64_2)

        val addr64_1 = Elf64Addr(0x1000UL)
        val addr64_2 = Elf64Addr(0x2000UL)
        assertTrue(addr64_1 < addr64_2)

        val off64_1 = Elf64Off(0x100UL)
        val off64_2 = Elf64Off(0x200UL)
        assertTrue(off64_1 < off64_2)
    }

    @Test
    fun testSameTypeEdgeCases() {
        // 测试边界值
        val minHalf = Elf32Half(UShort.MIN_VALUE)
        val maxHalf = Elf32Half(UShort.MAX_VALUE)
        assertTrue(minHalf < maxHalf)

        val minWord = Elf32Word(UInt.MIN_VALUE)
        val maxWord = Elf32Word(UInt.MAX_VALUE)
        assertTrue(minWord < maxWord)

        val minSword = Elf32Sword(Int.MIN_VALUE)
        val maxSword = Elf32Sword(Int.MAX_VALUE)
        assertTrue(minSword < maxSword)

        // 64位类型的边界值
        val minHalf64 = Elf64Half(UShort.MIN_VALUE)
        val maxHalf64 = Elf64Half(UShort.MAX_VALUE)
        assertTrue(minHalf64 < maxHalf64)

        val minWord64 = Elf64Word(UInt.MIN_VALUE)
        val maxWord64 = Elf64Word(UInt.MAX_VALUE)
        assertTrue(minWord64 < maxWord64)

        val minXword64 = Elf64Xword(ULong.MIN_VALUE)
        val maxXword64 = Elf64Xword(ULong.MAX_VALUE)
        assertTrue(minXword64 < maxXword64)

        val minSxword64 = Elf64Sxword(Long.MIN_VALUE)
        val maxSxword64 = Elf64Sxword(Long.MAX_VALUE)
        assertTrue(minSxword64 < maxSxword64)
    }
} 