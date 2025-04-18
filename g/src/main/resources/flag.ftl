@file:JvmName("-${typename}")
@file:Suppress("MemberVisibilityCanBePrivate", "unused", "RemoveRedundantCallsOfConversionMethods", "UnusedImport", "RemoveRedundantQualifierName")
package ${package}

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmName
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.longOrNull


fun ${typename}(value: U${rawType}) = ${typename}(value.to${rawType}())

/**
* Type for C flag-set ${typename}
*
* Raw type: [${rawType}]
*/
@OptIn(ExperimentalStdlibApi::class)
@JvmInline
@Serializable(with = ${typename}.Serializer::class)
value class ${typename}(val value: ${rawType}): Set<${typename}>{
    object Constants{
    <#list list as item>
        /**
        * ${item.docs}
        *
        * Raw value: `${item.value}`
        */
        const val ${item.name}: ${rawType} = ${item.value}.to${rawType}()
    </#list>
    }
    companion object{
        val ZERO = ${typename}(0.to${rawType}())
    <#list list as item>
        /**
        * ${item.docs}
        *
        * Raw value: `${item.value}`
        */
        val ${item.name}: ${typename} = ${typename}(Constants.${item.name})
    </#list>

/**
* Creates a ${typename} from its name
*
* @param name the name of the flag value
* @return the flag value, null if the name is not a known value
*/
        @JvmStatic
        fun valueOfOrNull(name: String): ${typename}? = when(name){
            <#list list as item>
            "${item.name}" -> ${item.name}
            </#list>
            else -> null
        }

/**
* Creates a ${typename} from a string
*
* The input string might be in one of the following formats:
*   - The name of the flag value.
*   - The hexadecimal representation of the flag value, prefixed with "0x".
* @param name the name of the flag value, or the text representation
* @return the flag value
* @throws IllegalArgumentException neither a known flag value nor a valid hexadecimal number
*/
        @JvmStatic
        fun valueOf(name: String): ${typename} = when(name){
            <#list list as item>
            "${item.name}" -> ${item.name}
            </#list>
            else -> run {
                val s = name.removePrefix("${typename}").removeSurrounding("(", ")")
                if(!s.startsWith("0x")) throw IllegalArgumentException("Invalid ${typename} value: should be a known value or a hex number(with \"0x\" prefix)")
                ${typename}(s.removePrefix("0x").toU${rawType}(16))
            }
        }
    }

    override fun toString(): String {
        if (size == 1) {
            return when (value) {
            <#list list as item>
                Constants.${item.name} -> "${item.name}"
            </#list>
                else -> "0x" + value.toU${rawType}().toHexString()
            }
        }
        return joinToString(" | ")
    }

    override val size: Int
        get() = value.countOneBits()

/**
 * Returns a new ${typename} with the specified bit set to true
 * @param bit the bit to set
 * @return a new ${typename} with the specified bit set to true
 * @see or
 */
    operator fun plus(bit: ${typename}): ${typename} {
        return ${typename}(value or bit.value)
    }

/**
 * Returns a new ElfPFlags with the specified bit set to true
 * @param bit the bit to set
 * @return a new ElfPFlags with the specified bit set to true
 */
infix fun or(bit: ${typename}): ${typename} = plus(bit)

    override fun iterator(): Iterator<${typename}> = object : Iterator<${typename}> {
        var remaining = value.to${rawType}()
        override fun hasNext(): Boolean = remaining != 0.to${rawType}()
        override fun next(): ${typename} {
            val bit = remaining and (-remaining).to${rawType}()
            remaining = (remaining xor bit).to${rawType}()
            return ${typename}(bit)
        }
    }

    override fun isEmpty(): Boolean = value == 0.to${rawType}()

    override fun containsAll(elements: Collection<${typename}>): Boolean {
        if (elements is ${typename}) {
            contains(elements)
        }
        return elements.all { value and it.value == it.value }
    }

    override fun contains(element: ${typename}): Boolean {
        return value and element.value == element.value
    }

internal object Serializer : KSerializer<${typename}> {
override val descriptor: SerialDescriptor =
SerialDescriptor("${package}.${typename}TGenerated", serialDescriptor<List<String>>())

    override fun deserialize(decoder: Decoder): ${typename} {
    var acc: ${typename} = ${typename}.ZERO
    try {
    if (decoder is JsonDecoder) {
    val e = decoder.decodeJsonElement()
    if (e is JsonArray) {
    for (it in e) {
    val p = it.jsonPrimitive
    acc += if (p.isString) {
    valueOf(p.content)
    } else {
    ${typename}(p.long.toU${rawType}())
    }
    }
    return acc
    }
    val p = e.jsonPrimitive
    return if (p.isString) {
    valueOf(p.content)
    } else {
    ${typename}(p.long.toU${rawType}())
    }
    }
    val dec = decoder.beginStructure(descriptor)
    while (true) {
    val i = dec.decodeElementIndex(descriptor)
    if (i == CompositeDecoder.DECODE_DONE) break
    val s = dec.decodeStringElement(descriptor, i)
    acc += valueOf(s)
    }
    dec.endStructure(descriptor)
    return acc
    } catch (e: Exception) {
    when (e) {
    is IllegalArgumentException, is NumberFormatException -> throw SerializationException("Deserialize JSON value failed: " + e.message)
    else -> throw e
    }
    }
    }

    override fun serialize(encoder: Encoder, value: ${typename}) {
    val enc = encoder.beginCollection(descriptor, value.size)
    for((i, it) in value.withIndex()){
    enc.encodeStringElement(descriptor, i, it.toString())
    }
    enc.endStructure(descriptor)
    }
    }

}
