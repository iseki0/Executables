@file:JvmName("-U")
package space.iseki.executables.elf

import kotlin.jvm.JvmName

internal fun ByteArray.u1(offset: Int): UByte = (this[offset].toUInt() and 0xFFu).toUByte()

internal fun ByteArray.u2l(offset: Int): UShort =
    ((this[offset].toUInt() and 0xFFu) or
            ((this[offset + 1].toUInt() and 0xFFu) shl 8)).toUShort()

internal fun ByteArray.u4e(offset: Int): UInt =
    ((this[offset].toUInt() and 0xFFu) or
            ((this[offset + 1].toUInt() and 0xFFu) shl 8) or
            ((this[offset + 2].toUInt() and 0xFFu) shl 16) or
            ((this[offset + 3].toUInt() and 0xFFu) shl 24))

internal fun ByteArray.u8l(offset: Int): ULong =
    ((this[offset].toULong() and 0xFFu) or
            ((this[offset + 1].toULong() and 0xFFu) shl 8) or
            ((this[offset + 2].toULong() and 0xFFu) shl 16) or
            ((this[offset + 3].toULong() and 0xFFu) shl 24) or
            ((this[offset + 4].toULong() and 0xFFu) shl 32) or
            ((this[offset + 5].toULong() and 0xFFu) shl 40) or
            ((this[offset + 6].toULong() and 0xFFu) shl 48) or
            ((this[offset + 7].toULong() and 0xFFu) shl 56))

internal fun ByteArray.u2b(offset: Int): UShort =
    (((this[offset].toUInt() and 0xFFu) shl 8) or
            (this[offset + 1].toUInt() and 0xFFu)).toUShort()

internal fun ByteArray.u4b(offset: Int): UInt =
    (((this[offset].toUInt() and 0xFFu) shl 24) or
            ((this[offset + 1].toUInt() and 0xFFu) shl 16) or
            ((this[offset + 2].toUInt() and 0xFFu) shl 8) or
            (this[offset + 3].toUInt() and 0xFFu))

internal fun ByteArray.u8b(offset: Int): ULong =
    (((this[offset].toULong() and 0xFFu) shl 56) or
            ((this[offset + 1].toULong() and 0xFFu) shl 48) or
            ((this[offset + 2].toULong() and 0xFFu) shl 40) or
            ((this[offset + 3].toULong() and 0xFFu) shl 32) or
            ((this[offset + 4].toULong() and 0xFFu) shl 24) or
            ((this[offset + 5].toULong() and 0xFFu) shl 16) or
            ((this[offset + 6].toULong() and 0xFFu) shl 8) or
            (this[offset + 7].toULong() and 0xFFu))
