@file:JvmName("-Utils")

package space.iseki.executables.pe

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlin.jvm.JvmName

internal fun decodeLongHex(decoder: Decoder, name: String): Long {
    val hex = decoder.decodeString()
    if (!hex.startsWith("0x")) {
        throw SerializationException("$name should start with 0x")
    }
    try {
        return hex.removePrefix("0x").toLong(16)
    } catch (e: NumberFormatException) {
        throw SerializationException("$name should be a hex string")
    }
}

