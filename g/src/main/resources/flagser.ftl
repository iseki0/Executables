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

internal object ${typename}Serializer : KSerializer<${typename}> {
    override val descriptor: SerialDescriptor
        get() = serialDescriptor<List<String>>()

    override fun deserialize(decoder: Decoder): ${typename} {
        var acc: ${typename} = ${typename}.ZERO
        val dec = decoder.beginStructure(descriptor)
        while (true){
            val i = dec.decodeElementIndex(descriptor)
            if (i == CompositeDecoder.DECODE_DONE) break
            val s = dec.decodeStringElement(descriptor, i)
            try{
                acc += ${typename}.valueOf(s)
            }catch(e: Exception){
                if(e !is IllegalArgumentException && e !is NumberFormatException) throw e
                throw SerializationException("Invalid ${typename} value: "+e.message, e)
            }
        }
        dec.endStructure(descriptor)
        return acc
    }

    override fun serialize(encoder: Encoder, value: ${typename}) {
        val enc = encoder.beginCollection(descriptor, value.size)
        for((i, it) in value.withIndex()){
            enc.encodeStringElement(descriptor, i, it.toString())
        }
        enc.endStructure(descriptor)
    }
}
