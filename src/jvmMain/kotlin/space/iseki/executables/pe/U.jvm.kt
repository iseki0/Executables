@file:JvmName("-U")

package space.iseki.executables.pe

import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.nio.ByteOrder

private val SH: VarHandle =
    MethodHandles.byteArrayViewVarHandle(ShortArray::class.java, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior()
private val IH: VarHandle =
    MethodHandles.byteArrayViewVarHandle(IntArray::class.java, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior()
private val LH: VarHandle =
    MethodHandles.byteArrayViewVarHandle(LongArray::class.java, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior()

internal actual fun ByteArray.getUShort(offset: Int): UShort {
    return (SH.get(this, offset) as Short).toUShort()
}

internal actual fun ByteArray.getUInt(offset: Int): UInt {
    return (IH.get(this, offset) as Int).toUInt()
}

internal actual fun ByteArray.getULong(offset: Int): ULong {
    return (LH.get(this, offset) as Long).toULong()
}

internal actual fun <T> Array<T>.toUnmodifiableList(): List<T> = java.util.Collections.unmodifiableList(this.asList())