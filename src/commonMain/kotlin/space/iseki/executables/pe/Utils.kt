@file:JvmName("-Utils")

package space.iseki.executables.pe

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
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

internal abstract class BitSetSerializer<T : Set<T>>(
    private val max: ULong,
    private val name: String,
    private val plusFn: (T, T) -> T,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor
        get() = serialDescriptor<List<String>>()

    protected abstract val unit: T
    protected abstract fun valueOfOrNull(element: String): T?
    protected abstract fun valueOf(element: ULong): T
    private fun valueOf0(element: String): T {
        return valueOfOrNull(element) ?: run {
            if (!element.startsWith("0x")) {
                throw SerializationException("$name should be a predefined value or a hex value")
            }
            val v = try {
                element.substring(2..element.lastIndex).toULong()
            } catch (e: NumberFormatException) {
                throw SerializationException("$name should be a predefined value or a hex value", e)
            }
            if (v > max) {
                throw SerializationException("hex value too large for a $name")
            }
            valueOf(v)
        }
    }

    override fun deserialize(decoder: Decoder): T = decoder.decodeStructure(serialDescriptor<List<String>>()) {
        var r = unit
        while (true) {
            val index = decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break
            r = plusFn(r, valueOf0(decodeStringElement(descriptor, index)))
        }
        r
    }


    override fun serialize(encoder: Encoder, value: T) {
        encoder.beginCollection(serialDescriptor<List<String>>(), value.size).apply {
            for ((index, characteristics) in value.withIndex()) {
                encodeStringElement(descriptor, index, characteristics.toString())
            }
            endStructure(descriptor)
        }
    }
}
