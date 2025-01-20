package space.iseki.executables.pe.serialization

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
import space.iseki.executables.pe.SectionFlags
import space.iseki.executables.pe.SectionTableItem

internal object SectionTableItemSerializer : KSerializer<SectionTableItem> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("space.iseki.executables.pe.SectionTableItem") {
            element<String>("name")
            element<UInt>("virtualSize")
            element("virtualAddress", Address32.serializer().descriptor)
            element<UInt>("sizeOfRawData")
            element("pointerToRawData", Address32.serializer().descriptor)
            element("pointerToRelocations", Address32.serializer().descriptor)
            element("pointerToLinenumbers", Address32.serializer().descriptor)
            element<UShort>("numberOfRelocations")
            element<UShort>("numberOfLinenumbers")
            element("characteristics", SectionFlagsSerializer.descriptor)
        }

    override fun serialize(encoder: Encoder, value: SectionTableItem) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name)
            encodeSerializableElement(descriptor, 1, UInt.serializer(), value.virtualSize)
            encodeSerializableElement(descriptor, 2, Address32.serializer(), value.virtualAddress)
            encodeSerializableElement(descriptor, 3, UInt.serializer(), value.sizeOfRawData)
            encodeSerializableElement(descriptor, 4, Address32.serializer(), value.pointerToRawData)
            encodeSerializableElement(descriptor, 5, Address32.serializer(), value.pointerToRelocations)
            encodeSerializableElement(descriptor, 6, Address32.serializer(), value.pointerToLinenumbers)
            encodeSerializableElement(descriptor, 7, UShort.serializer(), value.numberOfRelocations)
            encodeSerializableElement(descriptor, 8, UShort.serializer(), value.numberOfLinenumbers)
            encodeSerializableElement(descriptor, 9, SectionFlagsSerializer, value.characteristics)
        }
    }

    override fun deserialize(decoder: Decoder): SectionTableItem {
        return decoder.decodeStructure(descriptor) {
            var name = ""
            var virtualSize = 0u
            var virtualAddress = Address32(0u)
            var sizeOfRawData = 0u
            var pointerToRawData = Address32(0u)
            var pointerToRelocations = Address32(0u)
            var pointerToLinenumbers = Address32(0u)
            var numberOfRelocations = 0u.toUShort()
            var numberOfLinenumbers = 0u.toUShort()
            var characteristics = SectionFlags(0)

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> name = decodeStringElement(descriptor, 0)
                    1 -> virtualSize = decodeSerializableElement(descriptor, 1, UInt.serializer())
                    2 -> virtualAddress = decodeSerializableElement(descriptor, 2, Address32.serializer())
                    3 -> sizeOfRawData = decodeSerializableElement(descriptor, 3, UInt.serializer())
                    4 -> pointerToRawData = decodeSerializableElement(descriptor, 4, Address32.serializer())
                    5 -> pointerToRelocations = decodeSerializableElement(descriptor, 5, Address32.serializer())
                    6 -> pointerToLinenumbers = decodeSerializableElement(descriptor, 6, Address32.serializer())
                    7 -> numberOfRelocations = decodeSerializableElement(descriptor, 7, UShort.serializer())
                    8 -> numberOfLinenumbers = decodeSerializableElement(descriptor, 8, UShort.serializer())
                    9 -> characteristics = decodeSerializableElement(descriptor, 9, SectionFlagsSerializer)
                    else -> error("Unexpected index: $index")
                }
            }

            SectionTableItem(
                name = name,
                virtualSize = virtualSize,
                virtualAddress = virtualAddress,
                sizeOfRawData = sizeOfRawData,
                pointerToRawData = pointerToRawData,
                pointerToRelocations = pointerToRelocations,
                pointerToLinenumbers = pointerToLinenumbers,
                numberOfRelocations = numberOfRelocations,
                numberOfLinenumbers = numberOfLinenumbers,
                characteristics = characteristics
            )
        }
    }
} 