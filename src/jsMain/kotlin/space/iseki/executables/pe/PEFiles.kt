package space.iseki.executables.pe

import kotlinx.serialization.json.Json


@OptIn(ExperimentalJsExport::class)
@JsExport
fun dumpHeaderJson(data: ByteArray, pretty: Boolean = false): String {
    return (if (pretty) Json { prettyPrint = true } else Json).encodeToString(PEFile.wrap(data).summary)
}
