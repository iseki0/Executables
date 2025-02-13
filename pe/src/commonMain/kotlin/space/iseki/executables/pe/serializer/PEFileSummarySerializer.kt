package space.iseki.executables.pe.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import space.iseki.executables.pe.CoffHeader
import space.iseki.executables.pe.PEFile
import space.iseki.executables.pe.SectionTableItem
import space.iseki.executables.pe.StandardHeader
import space.iseki.executables.pe.WindowsSpecifiedHeader

internal object PEFileSummarySerializer : KSerializer<PEFile.Summary> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("space.iseki.executables.pe.PEFile.Summary") {
            element("coffHeader", CoffHeaderSerializer.descriptor)
            element("standardHeader", StandardHeaderSerializer.descriptor)
            element("windowsHeader", WindowsSpecifiedHeaderSerializer.descriptor)
            element("sectionTable", ListSerializer(SectionTableItemSerializer).descriptor)
        }

    override fun serialize(encoder: Encoder, value: PEFile.Summary) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, CoffHeaderSerializer, value.coffHeader)
            encodeSerializableElement(descriptor, 1, StandardHeaderSerializer, value.standardHeader)
            encodeSerializableElement(descriptor, 2, WindowsSpecifiedHeaderSerializer, value.windowsHeader)
            encodeSerializableElement(descriptor, 3, ListSerializer(SectionTableItemSerializer), value.sectionTable)
        }
    }

    override fun deserialize(decoder: Decoder): PEFile.Summary {
        return decoder.decodeStructure(descriptor) {
            var coffHeader: CoffHeader? = null
            var standardHeader: StandardHeader? = null
            var windowsHeader: WindowsSpecifiedHeader? = null
            var sectionTable: List<SectionTableItem>? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> coffHeader = decodeSerializableElement(descriptor, 0, CoffHeaderSerializer)
                    1 -> standardHeader = decodeSerializableElement(descriptor, 1, StandardHeaderSerializer)
                    2 -> windowsHeader = decodeSerializableElement(descriptor, 2, WindowsSpecifiedHeaderSerializer)
                    3 -> sectionTable =
                        decodeSerializableElement(descriptor, 3, ListSerializer(SectionTableItemSerializer))

                    else -> error("Unexpected index: $index")
                }
            }

            requireNotNull(coffHeader) { "coffHeader is required" }
            requireNotNull(standardHeader) { "standardHeader is required" }
            requireNotNull(windowsHeader) { "windowsHeader is required" }
            requireNotNull(sectionTable) { "sectionTable is required" }

            PEFile.Summary(
                coffHeader = coffHeader,
                standardHeader = standardHeader,
                windowsHeader = windowsHeader,
                sectionTable = sectionTable
            )
        }
    }
} 