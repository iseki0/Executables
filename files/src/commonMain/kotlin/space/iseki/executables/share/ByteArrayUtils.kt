@file:JvmName("ByteArrayUtilsKt0")
package space.iseki.executables.share

import kotlin.jvm.JvmName


/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned byte at the specified offset.
 */
internal expect fun ByteArray.u1(offset: Int): UByte

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned short in little-endian order at the specified offset.
 */
internal expect fun ByteArray.u2l(offset: Int): UShort

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned int in little-endian order at the specified offset.
 */
internal expect fun ByteArray.u4l(offset: Int): UInt

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned long in little-endian order at the specified offset.
 */
internal expect fun ByteArray.u8l(offset: Int): ULong

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned short in big-endian order at the specified offset.
 */
internal expect fun ByteArray.u2b(offset: Int): UShort

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned int in big-endian order at the specified offset.
 */
internal expect fun ByteArray.u4b(offset: Int): UInt

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned long in big-endian order at the specified offset.
 */
internal expect fun ByteArray.u8b(offset: Int): ULong

internal fun ByteArray.u2(offset: Int, littleEndian: Boolean): UShort = if (littleEndian) u2l(offset) else u2b(offset)
internal fun ByteArray.u4(offset: Int, littleEndian: Boolean): UInt = if (littleEndian) u4l(offset) else u4b(offset)
internal fun ByteArray.u8(offset: Int, littleEndian: Boolean): ULong = if (littleEndian) u8l(offset) else u8b(offset)
