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
import kotlinx.serialization.json.longOrNull


fun ${typename}(value: U${rawType}) = ${typename}(value.to${rawType}())

/**
* Type for C enum ${typename}
*
* Raw type: [${rawType}]
*/
@OptIn(ExperimentalStdlibApi::class)
@JvmInline
@Serializable(with = ${typename}.Serializer::class)
value class ${typename}(val value: ${rawType}) : Comparable<${typename}>{

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
/**
* Creates a ${typename} from a string
*
* The input string might be in one of the following formats:
*   - The name of the enum value.
*   - The hexadecimal representation of the enum value, prefixed with "0x".
* @param name the name of the enum value, or the text representation
* @return the enum value
* @throws IllegalArgumentException neither a known enum value nor a valid hexadecimal number
*/
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

/**
* Creates a ${typename} from its name
*
* @param name the name of the enum value
* @return the enum value, null if the name is not a known value
*
*/
        fun valueOfOrNull(name: String): ${typename}? = when(name){
        <#list list as item>
            "${item.name}" -> ${item.name}
        </#list>
            else -> null
        }

    <#list list as item>
        /**
        * ${item.docs}
        *
        * Raw value: `${item.value}`
        */
        val ${item.name}: ${typename} = ${typename}(Constants.${item.name})
    </#list>
    }

override fun compareTo(other: ${typename}): Int = value.toU${rawType}().compareTo(other.value.toU${rawType}())

    override fun toString(): String = when(value){
    <#list list as item>
        Constants.${item.name} -> "${item.name}"
    </#list>
        else -> "0x" + value.toU${rawType}().toHexString()
    }

internal object Serializer: KSerializer<${typename}>{
override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("${package}.${typename}TGenerated", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ${typename} {
    try{
    if (decoder is JsonDecoder){
    val e = decoder.decodeJsonElement().jsonPrimitive
    if(!e.isString){
    e.longOrNull?.let {return ${typename}(it.toU${rawType}()) }
    }
    return valueOf(e.content)
    }
    return valueOf(decoder.decodeString())
    }catch(e: Exception){
    if(e !is IllegalArgumentException && e !is NumberFormatException) throw e
    throw SerializationException("Invalid ${typename} value: "+e.message, e)
    }
    }

    override fun serialize(encoder: Encoder, value: ${typename}) {
    encoder.encodeString(value.toString())
    }
    }
}
