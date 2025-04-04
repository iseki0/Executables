package space.iseki.executables.pe

import space.iseki.executables.common.Address32
import space.iseki.executables.common.ExportSymbol

/**
 * Implementation of export symbol for PE format
 *
 * @property name The name of the export symbol
 * @property ordinal The ordinal number of the export symbol
 * @property address The relative virtual address of the export symbol
 * @property isForwarder Whether this symbol is a forwarder to another DLL
 * @property forwarderString The forwarder string if this symbol is a forwarder
 */
data class PEExportSymbol internal constructor(
    override val name: String,
    val ordinal: UShort,
    val address: Address32,
    val isForwarder: Boolean = false,
    val forwarderString: String? = null,
) : ExportSymbol {
    override val fields: Map<String, Any>
        get() = buildMap {
            put("name", name)
            put("ordinal", ordinal)
            put("address", address)
            put("isForwarder", isForwarder)
            if (forwarderString != null) put("forwarderString", forwarderString)
        }
}
