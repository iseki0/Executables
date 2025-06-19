package space.iseki.executables.pe

import space.iseki.executables.common.ImportSymbol
import kotlin.jvm.JvmName

/**
 * Implementation of import symbol for PE format
 *
 * @property name The name of the import symbol
 * @property file The DLL file name that contains this import symbol
 * @property ordinal The ordinal number of the import symbol (if imported by ordinal)
 * @property isOrdinal Whether this symbol is imported by ordinal
 */
data class PEImportSymbol private constructor(
    override val name: String,
    override val file: String,
    private val _ordinal: UShort = 0u,
    val isOrdinal: Boolean = false,
) : ImportSymbol {
    constructor(
        name: String,
        file: String,
        ordinal: UShort? = null,
        isOrdinal: Boolean = false,
    ) : this(
        name = name,
        file = file,
        _ordinal = if (isOrdinal) {
            requireNotNull(ordinal)
        } else {
            require(ordinal == null) { "ordinal must be null if isOrdinal is false" }
            0u
        },
        isOrdinal = isOrdinal,
    )

    @JvmName("component3-XRpZGF0")
    fun `_component3-XRpZGF0`(): UShort? = ordinal

    val ordinal: UShort? get() = if (isOrdinal) _ordinal else null

    override val fields: Map<String, Any>
        get() = buildMap {
            put("name", name)
            put("file", file)
            if (isOrdinal) put("ordinal", _ordinal)
            put("isOrdinal", isOrdinal)
        }
}
