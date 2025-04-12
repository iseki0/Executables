package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoLoadCommandType


interface MachoLoadCommand {
    val size: UInt

    val type: MachoLoadCommandType

}

