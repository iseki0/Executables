package space.iseki.executables.common

/**
 * Represents an imported symbol in an executable file
 *
 * @property name The name of the imported symbol
 * @property file The name of the file (library) from which the symbol is imported
 */
interface ImportSymbol : ReadableStructure {
    val name: String
    val file: String
}

/**
 * Represents a container that holds import symbols
 *
 * This interface is implemented by executable file formats that support
 * importing symbols from external libraries.
 *
 * @property importSymbols The list of import symbols in this container
 */
interface ImportSymbolContainer {
    val importSymbols: List<ImportSymbol>
}
