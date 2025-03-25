@file:JvmName("-Utils")

package space.iseki.executables.elf

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlin.jvm.JvmName

internal fun decodeULongHex(decoder: Decoder, name: String): ULong {
    val hex = decoder.decodeString()
    if (!hex.startsWith("0x")) {
        throw SerializationException("$name should start with 0x")
    }
    try {
        return hex.removePrefix("0x").toULong(16)
    } catch (e: NumberFormatException) {
        throw SerializationException("$name should be a hex string")
    }
}

internal fun decodeUIntHex(decoder: Decoder, name: String): UInt {
    val hex = decoder.decodeString()
    if (!hex.startsWith("0x")) {
        throw SerializationException("$name should start with 0x")
    }
    try {
        return hex.removePrefix("0x").toUInt(16)
    } catch (e: NumberFormatException) {
        throw SerializationException("$name should be a hex string")
    }
}
