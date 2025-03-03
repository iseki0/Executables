package space.iseki.executables.pe

import space.iseki.executables.common.ImportSymbol

/**
 * Implementation of import symbol for PE format
 *
 * @property name The name of the import symbol
 * @property file The DLL file name that contains this import symbol
 * @property ordinal The ordinal number of the import symbol (if imported by ordinal)
 * @property isOrdinal Whether this symbol is imported by ordinal
 */
data class PEImportSymbol(
    override val name: String,
    override val file: String,
    val ordinal: UShort? = null,
    val isOrdinal: Boolean = false,
) : ImportSymbol {
    override val fields: Map<String, Any>
        get() = buildMap {
            put("name", name)
            put("file", file)
            if (ordinal != null) put("ordinal", ordinal)
            put("isOrdinal", isOrdinal)
        }
}