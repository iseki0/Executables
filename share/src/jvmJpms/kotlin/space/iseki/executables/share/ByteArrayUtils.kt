@file:JvmName("-ByteArrayUtils")

package space.iseki.executables.share

import java.lang.invoke.MethodHandles
import java.nio.ByteOrder

private val SHORT_VIEW_LE = MethodHandles.byteArrayViewVarHandle(ShortArray::class.java, ByteOrder.LITTLE_ENDIAN)
private val SHORT_VIEW_BE = MethodHandles.byteArrayViewVarHandle(ShortArray::class.java, ByteOrder.BIG_ENDIAN)
private val INT_VIEW_LE = MethodHandles.byteArrayViewVarHandle(IntArray::class.java, ByteOrder.LITTLE_ENDIAN)
private val INT_VIEW_BE = MethodHandles.byteArrayViewVarHandle(IntArray::class.java, ByteOrder.BIG_ENDIAN)
private val LONG_VIEW_LE = MethodHandles.byteArrayViewVarHandle(LongArray::class.java, ByteOrder.LITTLE_ENDIAN)
private val LONG_VIEW_BE = MethodHandles.byteArrayViewVarHandle(LongArray::class.java, ByteOrder.BIG_ENDIAN)


/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned byte at the specified offset.
 */
fun ByteArray.u1(offset: Int): UByte = this[offset].toUByte()

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned short in little-endian order at the specified offset.
 */
fun ByteArray.u2l(offset: Int): UShort = (SHORT_VIEW_LE.get(this, offset) as Short).toUShort()

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned int in little-endian order at the specified offset.
 */
fun ByteArray.u4l(offset: Int): UInt = (INT_VIEW_LE.get(this, offset) as Int).toUInt()

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned long in little-endian order at the specified offset.
 */
fun ByteArray.u8l(offset: Int): ULong = (LONG_VIEW_LE.get(this, offset) as Long).toULong()

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned short in big-endian order at the specified offset.
 */
fun ByteArray.u2b(offset: Int): UShort = (SHORT_VIEW_BE.get(this, offset) as Short).toUShort()

/**
 * INTERNAL API - DO NOT USE OUTSIDE OF THIS PROJECT.
 *
 * Gets an unsigned int in big-endian order at the specified offset.
 */
fun ByteArray.u4b(offset: Int): UInt = (INT_VIEW_BE.get(this, offset) as Int).toUInt()


fun ByteArray.u8b(offset: Int): ULong = (LONG_VIEW_BE.get(this, offset) as Long).toULong()
