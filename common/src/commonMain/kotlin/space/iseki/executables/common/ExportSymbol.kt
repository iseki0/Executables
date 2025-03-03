package space.iseki.executables.common

interface ExportSymbol : ReadableStructure {
    val name: String
}

interface ExportSymbolContainer {
    val exportSymbols: List<ExportSymbol>
}
