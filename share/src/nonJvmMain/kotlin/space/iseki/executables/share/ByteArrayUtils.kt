package space.iseki.executables.share

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned byte at the specified offset.
 */
actual fun ByteArray.u1(offset: Int): UByte = (this[offset].toUInt() and 0xFFu).toUByte()

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned short in little-endian order at the specified offset.
 */
actual fun ByteArray.u2l(offset: Int): UShort =
    ((this[offset].toUInt() and 0xFFu) or ((this[offset + 1].toUInt() and 0xFFu) shl 8)).toUShort()

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned int in little-endian order at the specified offset.
 */
actual fun ByteArray.u4l(offset: Int): UInt =
    ((this[offset].toUInt() and 0xFFu) or ((this[offset + 1].toUInt() and 0xFFu) shl 8) or ((this[offset + 2].toUInt() and 0xFFu) shl 16) or ((this[offset + 3].toUInt() and 0xFFu) shl 24))

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned long in little-endian order at the specified offset.
 */
actual fun ByteArray.u8l(offset: Int): ULong =
    ((this[offset].toULong() and 0xFFu) or ((this[offset + 1].toULong() and 0xFFu) shl 8) or ((this[offset + 2].toULong() and 0xFFu) shl 16) or ((this[offset + 3].toULong() and 0xFFu) shl 24) or ((this[offset + 4].toULong() and 0xFFu) shl 32) or ((this[offset + 5].toULong() and 0xFFu) shl 40) or ((this[offset + 6].toULong() and 0xFFu) shl 48) or ((this[offset + 7].toULong() and 0xFFu) shl 56))

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned short in big-endian order at the specified offset.
 */
actual fun ByteArray.u2b(offset: Int): UShort =
    (((this[offset].toUInt() and 0xFFu) shl 8) or (this[offset + 1].toUInt() and 0xFFu)).toUShort()

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned int in big-endian order at the specified offset.
 */
actual fun ByteArray.u4b(offset: Int): UInt =
    (((this[offset].toUInt() and 0xFFu) shl 24) or ((this[offset + 1].toUInt() and 0xFFu) shl 16) or ((this[offset + 2].toUInt() and 0xFFu) shl 8) or (this[offset + 3].toUInt() and 0xFFu))

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned long in big-endian order at the specified offset.
 */
actual fun ByteArray.u8b(offset: Int): ULong =
    (((this[offset].toULong() and 0xFFu) shl 56) or ((this[offset + 1].toULong() and 0xFFu) shl 48) or ((this[offset + 2].toULong() and 0xFFu) shl 40) or ((this[offset + 3].toULong() and 0xFFu) shl 32) or ((this[offset + 4].toULong() and 0xFFu) shl 24) or ((this[offset + 5].toULong() and 0xFFu) shl 16) or ((this[offset + 6].toULong() and 0xFFu) shl 8) or (this[offset + 7].toULong() and 0xFFu)) 