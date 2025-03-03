package space.iseki.executables.share


/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned byte at the specified offset.
 */
expect fun ByteArray.u1(offset: Int): UByte

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned short in little-endian order at the specified offset.
 */
expect fun ByteArray.u2l(offset: Int): UShort

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned int in little-endian order at the specified offset.
 */
expect fun ByteArray.u4l(offset: Int): UInt

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned long in little-endian order at the specified offset.
 */
expect fun ByteArray.u8l(offset: Int): ULong

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned short in big-endian order at the specified offset.
 */
expect fun ByteArray.u2b(offset: Int): UShort

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned int in big-endian order at the specified offset.
 */
expect fun ByteArray.u4b(offset: Int): UInt

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned long in big-endian order at the specified offset.
 */
expect fun ByteArray.u8b(offset: Int): ULong