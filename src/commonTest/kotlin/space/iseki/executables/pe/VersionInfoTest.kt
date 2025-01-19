package space.iseki.executables.pe

import kotlin.test.Test
import kotlin.test.assertEquals

class VersionInfoTest {
    @Test
    fun testParse() {
        val bytes = ByteArray(VersionInfo.LENGTH) {
            when (it) {
                0 -> 0xEF.toByte()
                1 -> 0xFE.toByte()
                2 -> 0x00.toByte()
                3 -> 0x01.toByte()
                4 -> 0x02.toByte() // fileVersionMS low word
                5 -> 0x00.toByte()
                6 -> 0x01.toByte() // fileVersionMS high word
                7 -> 0x00.toByte()
                8 -> 0x04.toByte() // fileVersionLS low word
                9 -> 0x00.toByte()
                10 -> 0x03.toByte() // fileVersionLS high word
                11 -> 0x00.toByte()
                else -> 0x00.toByte()
            }
        }

        val info = VersionInfo.parse(bytes, 0)
        assertEquals(VersionInfo.SIGNATURE.toUShort(), info.signature)
        assertEquals(0x0100u, info.structVersion)
        assertEquals("1.2.3.4", info.fileVersion.toString())
    }
} 