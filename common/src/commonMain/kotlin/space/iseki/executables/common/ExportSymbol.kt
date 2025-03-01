package space.iseki.executables.common

interface ExportSymbol {
    val name: String
}

interface ExportSymbolContainer {
    val exportSymbols: List<ExportSymbol>
}
