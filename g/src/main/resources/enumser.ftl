@file:Suppress("unused")
package ${package}.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ${package}.${typename}

internal object ${typename}Serializer: KSerializer<${typename}>{
    override val descriptor: SerialDescriptor
        get() = serialDescriptor<String>()

    override fun deserialize(decoder: Decoder): ${typename} {
        try{
            return ${typename}.valueOf(decoder.decodeString())
        }catch(e: Exception){
            if(e !is IllegalArgumentException && e !is NumberFormatException) throw e
            throw SerializationException("Invalid ${typename} value: "+e.message, e)
        }
    }

    override fun serialize(encoder: Encoder, value: ${typename}) {
        encoder.encodeString(value.toString())
    }
}
