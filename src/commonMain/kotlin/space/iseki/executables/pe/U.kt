@file:JvmName(" U")
package space.iseki.executables.pe

import kotlin.jvm.JvmName

internal expect fun ByteArray.getUShort(offset: Int): UShort
internal expect fun ByteArray.getUInt(offset: Int): UInt
internal expect fun ByteArray.getULong(offset: Int): ULong
