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
import space.iseki.executables.pe.Characteristics
import space.iseki.executables.pe.CoffHeader
import space.iseki.executables.pe.MachineType
import space.iseki.executables.pe.TimeDataStamp32

internal object CoffHeaderSerializer : KSerializer<CoffHeader> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("space.iseki.executables.pe.CoffHeader") {
        element("machine", MachineTypeSerializer.descriptor)
        element<UShort>("numbersOfSections")
        element("timeDateStamp", TimeDataStamp32.serializer().descriptor)
        element("pointerToSymbolTable", Address32.serializer().descriptor)
        element<UInt>("numbersOfSymbols")
        element<UShort>("sizeOfOptionalHeader")
        element("characteristics", CharacteristicsSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, value: CoffHeader) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, MachineTypeSerializer, value.machine)
            encodeSerializableElement(descriptor, 1, UShort.serializer(), value.numbersOfSections)
            encodeSerializableElement(descriptor, 2, TimeDataStamp32.serializer(), value.timeDateStamp)
            encodeSerializableElement(descriptor, 3, Address32.serializer(), value.pointerToSymbolTable)
            encodeSerializableElement(descriptor, 4, UInt.serializer(), value.numbersOfSymbols)
            encodeSerializableElement(descriptor, 5, UShort.serializer(), value.sizeOfOptionalHeader)
            encodeSerializableElement(descriptor, 6, CharacteristicsSerializer, value.characteristics)
        }
    }

    override fun deserialize(decoder: Decoder): CoffHeader {
        return decoder.decodeStructure(descriptor) {
            var machine: MachineType? = null
            var numbersOfSections = 0u.toUShort()
            var timeDateStamp: TimeDataStamp32? = null
            var pointerToSymbolTable = Address32(0u)
            var numbersOfSymbols = 0u
            var sizeOfOptionalHeader = 0u.toUShort()
            var characteristics = Characteristics(0)

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> machine = decodeSerializableElement(descriptor, 0, MachineTypeSerializer)
                    1 -> numbersOfSections = decodeSerializableElement(descriptor, 1, UShort.serializer())
                    2 -> timeDateStamp = decodeSerializableElement(descriptor, 2, TimeDataStamp32.serializer())
                    3 -> pointerToSymbolTable = decodeSerializableElement(descriptor, 3, Address32.serializer())
                    4 -> numbersOfSymbols = decodeSerializableElement(descriptor, 4, UInt.serializer())
                    5 -> sizeOfOptionalHeader = decodeSerializableElement(descriptor, 5, UShort.serializer())
                    6 -> characteristics = decodeSerializableElement(descriptor, 6, CharacteristicsSerializer)
                    else -> error("Unexpected index: $index")
                }
            }

            requireNotNull(machine) { "machine is required" }
            requireNotNull(timeDateStamp) { "timeDateStamp is required" }

            CoffHeader(
                machine = machine,
                numbersOfSections = numbersOfSections,
                timeDateStamp = timeDateStamp,
                pointerToSymbolTable = pointerToSymbolTable,
                numbersOfSymbols = numbersOfSymbols,
                sizeOfOptionalHeader = sizeOfOptionalHeader,
                characteristics = characteristics
            )
        }
    }
} 