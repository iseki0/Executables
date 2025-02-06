package space.iseki.executables.pe.vi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
    @OptIn(ExperimentalStdlibApi::class)
    private val stringTableData = """
# 文本表
5E 02 00 00 01 00 30 00 34 00 30 00 39 00 30 00 
34 00 62 00 30 00 00 00 40 00 10 00 01 00 43 00 
6F 00 6D 00 70 00 61 00 6E 00 79 00 4E 00 61 00 
6D 00 65 00 00 00 00 00 41 00 6D 00 61 00 7A 00 
6F 00 6E 00 2E 00 63 00 6F 00 6D 00 20 00 49 00 
6E 00 63 00 2E 00 00 00 58 00 18 00 01 00 46 00 
69 00 6C 00 65 00 44 00 65 00 73 00 63 00 72 00 
69 00 70 00 74 00 69 00 6F 00 6E 00 00 00 00 00 
4F 00 70 00 65 00 6E 00 4A 00 44 00 4B 00 20 00 
50 00 6C 00 61 00 74 00 66 00 6F 00 72 00 6D 00 
20 00 62 00 69 00 6E 00 61 00 72 00 79 00 00 00 
32 00 09 00 01 00 46 00 69 00 6C 00 65 00 56 00 
65 00 72 00 73 00 69 00 6F 00 6E 00 00 00 00 00 
32 00 30 00 2E 00 30 00 2E 00 32 00 2E 00 31 00 
00 00 00 00 3E 00 0F 00 01 00 46 00 75 00 6C 00 
6C 00 20 00 56 00 65 00 72 00 73 00 69 00 6F 00 
6E 00 00 00 32 00 30 00 2E 00 30 00 2E 00 32 00 
2E 00 31 00 2B 00 31 00 30 00 2D 00 46 00 52 00 
00 00 00 00 2A 00 05 00 01 00 49 00 6E 00 74 00 
65 00 72 00 6E 00 61 00 6C 00 4E 00 61 00 6D 00 
65 00 00 00 6A 00 61 00 76 00 61 00 00 00 00 00 
46 00 11 00 01 00 4C 00 65 00 67 00 61 00 6C 00 
43 00 6F 00 70 00 79 00 72 00 69 00 67 00 68 00 
74 00 00 00 43 00 6F 00 70 00 79 00 72 00 69 00 
67 00 68 00 74 00 20 00 A9 00 20 00 32 00 30 00 
32 00 33 00 00 00 00 00 3A 00 09 00 01 00 4F 00 
72 00 69 00 67 00 69 00 6E 00 61 00 6C 00 46 00 
69 00 6C 00 65 00 6E 00 61 00 6D 00 65 00 00 00 
6A 00 61 00 76 00 61 00 2E 00 65 00 78 00 65 00 
00 00 00 00 54 00 1A 00 01 00 50 00 72 00 6F 00 
64 00 75 00 63 00 74 00 4E 00 61 00 6D 00 65 00 
00 00 00 00 4F 00 70 00 65 00 6E 00 4A 00 44 00 
4B 00 20 00 50 00 6C 00 61 00 74 00 66 00 6F 00 
72 00 6D 00 20 00 32 00 30 00 2E 00 30 00 2E 00 
32 00 2E 00 31 00 00 00 36 00 09 00 01 00 50 00 
72 00 6F 00 64 00 75 00 63 00 74 00 56 00 65 00 
72 00 73 00 69 00 6F 00 6E 00 00 00 32 00 30 00 
2E 00 30 00 2E 00 32 00 2E 00 31 00 00 00 00 00
# 不重要的剩余内容
44 00 00 00 01 00 56 00 61 00 72 00 46 00 69 00 
6C 00 65 00 49 00 6E 00 66 00 6F 00 00 00 00 00 
24 00 04 00 00 00 54 00 72 00 61 00 6E 00 73 00 
6C 00 61 00 74 00 69 00 6F 00 6E 00 00 00 00 00 
09 04 B0 04 
        """.trimIndent().replace(Regex("[\\r\\n ]|#.*$", RegexOption.MULTILINE), "").hexToByteArray()

    private val json = Json { prettyPrint = true }
    private val versionStringTable = """
        {
            "langKey": 67699888,
            "strings": [
                {
                    "first": "CompanyName",
                    "second": "Amazon.com Inc."
                },
                {
                    "first": "FileDescription",
                    "second": "OpenJDK Platform binary"
                },
                {
                    "first": "FileVersion",
                    "second": "20.0.2.1"
                },
                {
                    "first": "Full Version",
                    "second": "20.0.2.1+10-FR"
                },
                {
                    "first": "InternalName",
                    "second": "java"
                },
                {
                    "first": "LegalCopyright",
                    "second": "Copyright ${'\u00a9'} 2023"
                },
                {
                    "first": "OriginalFilename",
                    "second": "java.exe"
                },
                {
                    "first": "ProductName",
                    "second": "OpenJDK Platform 20.0.2.1"
                },
                {
                    "first": "ProductVersion",
                    "second": "20.0.2.1"
                }
            ]
        }
    """.trimIndent()

    @Test
    fun testParseStringTable() {
        val list = parseStringTable(stringTableData, 0)
        assertEquals(json.decodeFromString<JsonElement>(versionStringTable), json.encodeToJsonElement(list))
    }

}