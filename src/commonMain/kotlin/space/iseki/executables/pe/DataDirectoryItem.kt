package space.iseki.executables.pe

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@Serializable(with = DataDirectoryItem.Serializer::class)
@JvmInline
value class DataDirectoryItem(
    private val rawValue: Long,
) {
    object Serializer : KSerializer<DataDirectoryItem> {
        @Serializable
        private data class DTO(
            val virtualAddress: Address32,
            val size: UInt,
        )

        override val descriptor: SerialDescriptor = DTO.serializer().descriptor

        override fun deserialize(decoder: Decoder): DataDirectoryItem {
            decoder.decodeSerializableValue(DTO.serializer()).let {
                val v = it.virtualAddress.rawValue.toLong() shl 32 or (it.size.toLong() and 0xFFFFFFFFL)
                return DataDirectoryItem(v)
            }
        }

        override fun serialize(encoder: Encoder, value: DataDirectoryItem) {
            encoder.encodeSerializableValue(DTO.serializer(), DTO(Address32(value.virtualAddress.toUInt()), value.size.toUInt()))
        }

    }

    constructor(virtualAddress: Int, size: Int) : this((virtualAddress.toLong() shl 32) or size.toLong())

    companion object {
        val ZERO = DataDirectoryItem(0)

        operator fun invoke(virtualAddress: UInt, size: UInt): DataDirectoryItem {
            return DataDirectoryItem(virtualAddress.toInt(), size.toInt())
        }

        @JvmStatic
        fun toString(rawValue: Long): String {
            return DataDirectoryItem(rawValue).toString()
        }

        @JvmStatic
        fun parse(bytes: ByteArray, offset: Int): DataDirectoryItem {
            val virtualAddress = bytes.getUInt(offset)
            val size = bytes.getUInt(offset + 4)
            return DataDirectoryItem(virtualAddress, size)
        }
    }

    val virtualAddress: Int
        get() = (rawValue ushr 32).toInt()

    val size: Int
        get() = rawValue.toInt()

    override fun toString(): String {
        if (this == ZERO) {
            return "ZERO"
        }
        return "[${Address32(virtualAddress.toUInt())} + $size]"
    }
}

