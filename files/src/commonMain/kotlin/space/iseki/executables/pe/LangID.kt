package space.iseki.executables.pe

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@OptIn(ExperimentalStdlibApi::class)
@Serializable(with = LangID.Serializer::class)
@JvmInline
value class LangID(val value: UShort) {
    constructor(value: String) : this(
        try {
            LangID_rref[value] ?: value.removePrefix("0x").toUShort(16)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid LangID: $value")
        }
    )

    override fun toString(): String = LangID_ref[value] ?: "0x${value.toHexString()}"

    object Serializer : KSerializer<LangID> {
        override val descriptor = serialDescriptor<String>()
        override fun serialize(encoder: Encoder, value: LangID) = encoder.encodeString(value.toString())
        override fun deserialize(decoder: Decoder) = LangID(decoder.decodeString())
    }
}
