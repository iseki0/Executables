package space.iseki.executables.macho.lc

import space.iseki.executables.macho.MachoLoadCommandType

class InvalidLoaderCommandException(val cmd: MachoLoadCommandType, message: String) : Exception("$cmd: $message")