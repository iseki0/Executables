package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoLoadCommandType

data class UnsupportedCommand internal constructor(override val size: UInt, override val type: MachoLoadCommandType) :
    MachoLoadCommand
