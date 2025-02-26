package space.iseki.executables.pe

import kotlinx.serialization.json.Json


@OptIn(ExperimentalJsExport::class)
@JsExport
fun dumpHeaderJson(data: ByteArray, pretty: Boolean = false): String {
    val json = if (pretty) Json { prettyPrint = true } else Json
    return json.encodeToString(PEFile.open(data).summary)
}
