package space.iseki.executables.pe

import space.iseki.executables.common.ByteArrayDataAccessor

actual fun PEFile(bytes: ByteArray): PEFile = PEFile.open(ByteArrayDataAccessor(bytes))
