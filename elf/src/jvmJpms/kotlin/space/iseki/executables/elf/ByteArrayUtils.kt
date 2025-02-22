@file:JvmName("-U")

package space.iseki.executables.elf

import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.nio.ByteOrder

private val SLH: VarHandle = MethodHandles.byteArrayViewVarHandle(ShortArray::class.java, ByteOrder.LITTLE_ENDIAN)
private val ILH: VarHandle = MethodHandles.byteArrayViewVarHandle(IntArray::class.java, ByteOrder.LITTLE_ENDIAN)
private val LLH: VarHandle = MethodHandles.byteArrayViewVarHandle(LongArray::class.java, ByteOrder.LITTLE_ENDIAN)
private val SBH: VarHandle = MethodHandles.byteArrayViewVarHandle(ShortArray::class.java, ByteOrder.BIG_ENDIAN)
private val IBH: VarHandle = MethodHandles.byteArrayViewVarHandle(IntArray::class.java, ByteOrder.BIG_ENDIAN)
private val LBH: VarHandle = MethodHandles.byteArrayViewVarHandle(LongArray::class.java, ByteOrder.BIG_ENDIAN)

internal fun ByteArray.u1(offset: Int): UByte = (this[offset].toUInt() and 0xFFu).toUByte()

internal fun ByteArray.u2l(offset: Int): UShort = (SLH.get(this, offset) as Short).toUShort()
internal fun ByteArray.u4e(offset: Int): UInt = (ILH.get(this, offset) as Int).toUInt()
internal fun ByteArray.u8l(offset: Int): ULong = (LLH.get(this, offset) as Long).toULong()
internal fun ByteArray.u2b(offset: Int): UShort = (SBH.get(this, offset) as Short).toUShort()
internal fun ByteArray.u4b(offset: Int): UInt = (IBH.get(this, offset) as Int).toUInt()
internal fun ByteArray.u8b(offset: Int): ULong = (LBH.get(this, offset) as Long).toULong()