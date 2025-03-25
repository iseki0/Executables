@file:JvmName("Parser")

package space.iseki.executables.pe.vi

import kotlinx.serialization.Serializable
import space.iseki.executables.pe.getWString
import space.iseki.executables.share.toUnmodifiableList
import kotlin.jvm.JvmName

fun parseVersionData(bytes: ByteArray, off: Int): PEVersionInfo {
    var pos = off
    val header = parseStructureHeader(bytes, pos)
    pos += header.length
    header.check {
        expectSzKey("VS_VERSION_INFO")
        expectWType(0u)
    }
    val fixedFileInfo = if (header.wValueLength == 0.toUShort()) {
        null
    } else {
        FixedFileInfo.parse(bytes.sliceArray(pos until pos + header.wValueLength.toInt()), 0)
    }
    pos += header.wValueLength.toInt()
    pos += paddingAlignTo32Bit(pos)
    var stringTable: StringTable? = null
    while (pos < header.wLength.toInt()) {
        val nHeader = parseStructureHeader(bytes, pos)
        if (nHeader.szKey == "StringFileInfo") {
            pos += nHeader.length
            stringTable = parseStringTable(bytes, pos)
            pos -= nHeader.length
        }
        pos += nHeader.wLength.toInt()
        pos += paddingAlignTo32Bit(pos)
    }
    return PEVersionInfo(fixedFileInfo = fixedFileInfo, stringFileInfo = stringTable)
}

internal fun parseStringTable(bytes: ByteArray, off: Int) = parseStringTable(bytes, parseStructureHeader(bytes, off))
internal fun parseStringTable(bytes: ByteArray, header: StructureHeader): StringTable {
    val langKey: UInt
    header.check {
        expectWValueLength(0u)
        expectWType(1u)
        langKey = try {
            check(header.szKey.length == 8)
            header.szKey.toUInt(16)
        } catch (_: Exception) {
            failed("szKey", "should be a hex number with 8 characters")
            0u
        }
    }
    val list = buildList {
        var pos = header.offset + header.length
        val end = header.offset + header.wLength.toInt()
        while (pos < end) {
            val stringHeader = parseStructureHeader(bytes, pos)
            pos += stringHeader.length
            val value = if (stringHeader.wValueLength.toInt() == 0) {
                ""
            } else {
                bytes.getWString(pos, stringHeader.wValueLength.toInt() * 2 - 2)
            }
            pos += stringHeader.wValueLength.toInt() * 2
            add(stringHeader.szKey to value)
            pos += paddingAlignTo32Bit(pos)
        }
    }
    return StringTable(langKey, list.toUnmodifiableList())
}

@Serializable
class StringTable internal constructor(
    val langKey: UInt,
    val strings: List<Pair<String, String>>,
) {
    override fun toString(): String {
        return "StringTable(langKey=$langKey, strings=$strings)"
    }
}
