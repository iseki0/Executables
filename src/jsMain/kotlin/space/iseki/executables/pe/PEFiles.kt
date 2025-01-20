package space.iseki.executables.pe

import kotlinx.serialization.json.Json
import space.iseki.executables.pe.serialization.PEFileSummarySerializer


@OptIn(ExperimentalJsExport::class)
@JsExport
fun dumpHeaderJson(data: ByteArray, pretty: Boolean = false): String {
    val json = if (pretty) Json { prettyPrint = true } else Json
    return json.encodeToString(PEFileSummarySerializer, PEFile.wrap(data).summary)
}
