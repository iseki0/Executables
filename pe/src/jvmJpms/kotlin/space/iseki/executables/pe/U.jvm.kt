@file:JvmName("-U")

package space.iseki.executables.pe

import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.nio.ByteOrder

private val SH: VarHandle = MethodHandles.byteArrayViewVarHandle(ShortArray::class.java, ByteOrder.LITTLE_ENDIAN)
private val IH: VarHandle = MethodHandles.byteArrayViewVarHandle(IntArray::class.java, ByteOrder.LITTLE_ENDIAN)
private val LH: VarHandle = MethodHandles.byteArrayViewVarHandle(LongArray::class.java, ByteOrder.LITTLE_ENDIAN)

internal fun ByteArray.getUShort(offset: Int): UShort {
    return (SH.get(this, offset) as Short).toUShort()
}

internal fun ByteArray.getUInt(offset: Int): UInt {
    return (IH.get(this, offset) as Int).toUInt()
}

internal fun ByteArray.getULong(offset: Int): ULong {
    return (LH.get(this, offset) as Long).toULong()
}

internal fun <T> Array<T>.toUnmodifiableList(): List<T> = java.util.Collections.unmodifiableList(this.asList())
internal fun ByteArray.getWString(offset: Int, length: Int): String =
    java.lang.String(this, offset, length, Charsets.UTF_16LE) as String

internal fun <T> List<T>.toUnmodifiableList(): List<T> = java.util.Collections.unmodifiableList(this)
