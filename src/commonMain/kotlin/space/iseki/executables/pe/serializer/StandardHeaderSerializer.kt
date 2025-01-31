package space.iseki.executables.pe.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import space.iseki.executables.pe.Address32
import space.iseki.executables.pe.PE32Magic
import space.iseki.executables.pe.StandardHeader

internal object StandardHeaderSerializer : KSerializer<StandardHeader> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("space.iseki.executables.pe.StandardHeader") {
            element("magic", PE32MagicSerializer.descriptor)
            element<Byte>("majorLinkerVersion")
            element<Byte>("minorLinkerVersion")
            element<UInt>("sizeOfCode")
            element<UInt>("sizeOfInitializedData")
            element<UInt>("sizeOfUninitializedData")
            element("addressOfEntryPoint", Address32.serializer().descriptor)
            element("baseOfCode", Address32.serializer().descriptor)
            element("baseOfData", Address32.serializer().descriptor)
        }

    override fun serialize(encoder: Encoder, value: StandardHeader) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, PE32MagicSerializer, value.magic)
            encodeSerializableElement(descriptor, 1, Byte.serializer(), value.majorLinkerVersion)
            encodeSerializableElement(descriptor, 2, Byte.serializer(), value.minorLinkerVersion)
            encodeSerializableElement(descriptor, 3, UInt.serializer(), value.sizeOfCode)
            encodeSerializableElement(descriptor, 4, UInt.serializer(), value.sizeOfInitializedData)
            encodeSerializableElement(descriptor, 5, UInt.serializer(), value.sizeOfUninitializedData)
            encodeSerializableElement(descriptor, 6, Address32.serializer(), value.addressOfEntryPoint)
            encodeSerializableElement(descriptor, 7, Address32.serializer(), value.baseOfCode)
            encodeSerializableElement(descriptor, 8, Address32.serializer(), value.baseOfData)
        }
    }

    override fun deserialize(decoder: Decoder): StandardHeader {
        return decoder.decodeStructure(descriptor) {
            var magic: PE32Magic? = null
            var majorLinkerVersion: Byte = 0
            var minorLinkerVersion: Byte = 0
            var sizeOfCode = 0u
            var sizeOfInitializedData = 0u
            var sizeOfUninitializedData = 0u
            var addressOfEntryPoint = Address32(0u)
            var baseOfCode = Address32(0u)
            var baseOfData = Address32(0u)

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> magic = decodeSerializableElement(descriptor, 0, PE32MagicSerializer)
                    1 -> majorLinkerVersion = decodeSerializableElement(descriptor, 1, Byte.serializer())
                    2 -> minorLinkerVersion = decodeSerializableElement(descriptor, 2, Byte.serializer())
                    3 -> sizeOfCode = decodeSerializableElement(descriptor, 3, UInt.serializer())
                    4 -> sizeOfInitializedData = decodeSerializableElement(descriptor, 4, UInt.serializer())
                    5 -> sizeOfUninitializedData = decodeSerializableElement(descriptor, 5, UInt.serializer())
                    6 -> addressOfEntryPoint = decodeSerializableElement(descriptor, 6, Address32.serializer())
                    7 -> baseOfCode = decodeSerializableElement(descriptor, 7, Address32.serializer())
                    8 -> baseOfData = decodeSerializableElement(descriptor, 8, Address32.serializer())
                    else -> error("Unexpected index: $index")
                }
            }

            requireNotNull(magic) { "magic is required" }

            StandardHeader(
                magic = magic,
                majorLinkerVersion = majorLinkerVersion,
                minorLinkerVersion = minorLinkerVersion,
                sizeOfCode = sizeOfCode,
                sizeOfInitializedData = sizeOfInitializedData,
                sizeOfUninitializedData = sizeOfUninitializedData,
                addressOfEntryPoint = addressOfEntryPoint,
                baseOfCode = baseOfCode,
                baseOfData = baseOfData
            )
        }
    }
} 