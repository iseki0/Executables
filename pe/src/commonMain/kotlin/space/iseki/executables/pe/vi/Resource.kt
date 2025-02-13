package space.iseki.executables.pe.vi

import space.iseki.executables.pe.PEFile

internal fun locateVersionInfo(file: PEFile) = file.resourceRoot.listChildren()
    .firstOrNull { it.id == 16u }
    ?.listChildren()
    ?.firstOrNull { it.id == 1u }
    ?.listChildren()
    ?.firstOrNull()
