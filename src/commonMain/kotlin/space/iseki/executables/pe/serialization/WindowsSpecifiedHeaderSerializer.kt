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
import space.iseki.executables.pe.Address64
import space.iseki.executables.pe.DataDirectoryItem
import space.iseki.executables.pe.DllCharacteristics
import space.iseki.executables.pe.PE32Magic
import space.iseki.executables.pe.WindowsSpecifiedHeader
import space.iseki.executables.pe.WindowsSubsystems

internal object WindowsSpecifiedHeaderSerializer : KSerializer<WindowsSpecifiedHeader> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("space.iseki.executables.pe.WindowsSpecifiedHeader") {
            element("magic", PE32MagicSerializer.descriptor)
            element("imageBase", Address64.serializer().descriptor)
            element<UInt>("sectionAlignment")
            element<UInt>("fileAlignment")
            element<UShort>("majorOperatingSystemVersion")
            element<UShort>("minorOperatingSystemVersion")
            element<UShort>("majorImageVersion")
            element<UShort>("minorImageVersion")
            element<UShort>("majorSubsystemVersion")
            element<UShort>("minorSubsystemVersion")
            element<UInt>("win32VersionValue")
            element<UInt>("sizeOfImage")
            element<UInt>("sizeOfHeaders")
            element<UInt>("checkSum")
            element("subsystem", WindowsSubsystemsSerializer.descriptor)
            element("dllCharacteristics", DllCharacteristicsSerializer.descriptor)
            element<ULong>("sizeOfStackReserve")
            element<ULong>("sizeOfStackCommit")
            element<ULong>("sizeOfHeapReserve")
            element<ULong>("sizeOfHeapCommit")
            element<UInt>("loaderFlags")
            element<Int>("numbersOfRvaAndSizes")
            element("exportTable", DataDirectoryItem.serializer().descriptor)
            element("importTable", DataDirectoryItem.serializer().descriptor)
            element("resourceTable", DataDirectoryItem.serializer().descriptor)
            element("exceptionTable", DataDirectoryItem.serializer().descriptor)
            element("certificateTable", DataDirectoryItem.serializer().descriptor)
            element("baseRelocationTable", DataDirectoryItem.serializer().descriptor)
            element("debug", DataDirectoryItem.serializer().descriptor)
            element("architecture", DataDirectoryItem.serializer().descriptor)
            element("globalPtr", DataDirectoryItem.serializer().descriptor)
            element("tlsTable", DataDirectoryItem.serializer().descriptor)
            element("loadConfigTable", DataDirectoryItem.serializer().descriptor)
            element("boundImport", DataDirectoryItem.serializer().descriptor)
            element("iat", DataDirectoryItem.serializer().descriptor)
            element("delayImportDescriptor", DataDirectoryItem.serializer().descriptor)
            element("clrRuntimeHeader", DataDirectoryItem.serializer().descriptor)
        }

    override fun serialize(encoder: Encoder, value: WindowsSpecifiedHeader) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, PE32MagicSerializer, value.magic)
            encodeSerializableElement(descriptor, 1, Address64.serializer(), value.imageBase)
            encodeSerializableElement(descriptor, 2, UInt.serializer(), value.sectionAlignment)
            encodeSerializableElement(descriptor, 3, UInt.serializer(), value.fileAlignment)
            encodeSerializableElement(descriptor, 4, UShort.serializer(), value.majorOperatingSystemVersion)
            encodeSerializableElement(descriptor, 5, UShort.serializer(), value.minorOperatingSystemVersion)
            encodeSerializableElement(descriptor, 6, UShort.serializer(), value.majorImageVersion)
            encodeSerializableElement(descriptor, 7, UShort.serializer(), value.minorImageVersion)
            encodeSerializableElement(descriptor, 8, UShort.serializer(), value.majorSubsystemVersion)
            encodeSerializableElement(descriptor, 9, UShort.serializer(), value.minorSubsystemVersion)
            encodeSerializableElement(descriptor, 10, UInt.serializer(), value.win32VersionValue)
            encodeSerializableElement(descriptor, 11, UInt.serializer(), value.sizeOfImage)
            encodeSerializableElement(descriptor, 12, UInt.serializer(), value.sizeOfHeaders)
            encodeSerializableElement(descriptor, 13, UInt.serializer(), value.checkSum)
            encodeSerializableElement(descriptor, 14, WindowsSubsystemsSerializer, value.subsystem)
            encodeSerializableElement(descriptor, 15, DllCharacteristicsSerializer, value.dllCharacteristics)
            encodeSerializableElement(descriptor, 16, ULong.serializer(), value.sizeOfStackReserve)
            encodeSerializableElement(descriptor, 17, ULong.serializer(), value.sizeOfStackCommit)
            encodeSerializableElement(descriptor, 18, ULong.serializer(), value.sizeOfHeapReserve)
            encodeSerializableElement(descriptor, 19, ULong.serializer(), value.sizeOfHeapCommit)
            encodeSerializableElement(descriptor, 20, UInt.serializer(), value.loaderFlags)
            encodeIntElement(descriptor, 21, value.numbersOfRvaAndSizes)
            encodeSerializableElement(descriptor, 22, DataDirectoryItem.serializer(), value.exportTable)
            encodeSerializableElement(descriptor, 23, DataDirectoryItem.serializer(), value.importTable)
            encodeSerializableElement(descriptor, 24, DataDirectoryItem.serializer(), value.resourceTable)
            encodeSerializableElement(descriptor, 25, DataDirectoryItem.serializer(), value.exceptionTable)
            encodeSerializableElement(descriptor, 26, DataDirectoryItem.serializer(), value.certificateTable)
            encodeSerializableElement(descriptor, 27, DataDirectoryItem.serializer(), value.baseRelocationTable)
            encodeSerializableElement(descriptor, 28, DataDirectoryItem.serializer(), value.debug)
            encodeSerializableElement(descriptor, 29, DataDirectoryItem.serializer(), value.architecture)
            encodeSerializableElement(descriptor, 30, DataDirectoryItem.serializer(), value.globalPtr)
            encodeSerializableElement(descriptor, 31, DataDirectoryItem.serializer(), value.tlsTable)
            encodeSerializableElement(descriptor, 32, DataDirectoryItem.serializer(), value.loadConfigTable)
            encodeSerializableElement(descriptor, 33, DataDirectoryItem.serializer(), value.boundImport)
            encodeSerializableElement(descriptor, 34, DataDirectoryItem.serializer(), value.iat)
            encodeSerializableElement(descriptor, 35, DataDirectoryItem.serializer(), value.delayImportDescriptor)
            encodeSerializableElement(descriptor, 36, DataDirectoryItem.serializer(), value.clrRuntimeHeader)
        }
    }

    override fun deserialize(decoder: Decoder): WindowsSpecifiedHeader {
        return decoder.decodeStructure(descriptor) {
            var magic: PE32Magic? = null
            var imageBase: Address64? = null
            var sectionAlignment = 0u
            var fileAlignment = 0u
            var majorOperatingSystemVersion = 0u.toUShort()
            var minorOperatingSystemVersion = 0u.toUShort()
            var majorImageVersion = 0u.toUShort()
            var minorImageVersion = 0u.toUShort()
            var majorSubsystemVersion = 0u.toUShort()
            var minorSubsystemVersion = 0u.toUShort()
            var win32VersionValue = 0u
            var sizeOfImage = 0u
            var sizeOfHeaders = 0u
            var checkSum = 0u
            var subsystem: WindowsSubsystems? = null
            var dllCharacteristics = DllCharacteristics(0)
            var sizeOfStackReserve = 0uL
            var sizeOfStackCommit = 0uL
            var sizeOfHeapReserve = 0uL
            var sizeOfHeapCommit = 0uL
            var loaderFlags = 0u
            var numbersOfRvaAndSizes = 0
            var exportTable = DataDirectoryItem.ZERO
            var importTable = DataDirectoryItem.ZERO
            var resourceTable = DataDirectoryItem.ZERO
            var exceptionTable = DataDirectoryItem.ZERO
            var certificateTable = DataDirectoryItem.ZERO
            var baseRelocationTable = DataDirectoryItem.ZERO
            var debug = DataDirectoryItem.ZERO
            var architecture = DataDirectoryItem.ZERO
            var globalPtr = DataDirectoryItem.ZERO
            var tlsTable = DataDirectoryItem.ZERO
            var loadConfigTable = DataDirectoryItem.ZERO
            var boundImport = DataDirectoryItem.ZERO
            var iat = DataDirectoryItem.ZERO
            var delayImportDescriptor = DataDirectoryItem.ZERO
            var clrRuntimeHeader = DataDirectoryItem.ZERO

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> magic = decodeSerializableElement(descriptor, 0, PE32MagicSerializer)
                    1 -> imageBase = decodeSerializableElement(descriptor, 1, Address64.serializer())
                    2 -> sectionAlignment = decodeSerializableElement(descriptor, 2, UInt.serializer())
                    3 -> fileAlignment = decodeSerializableElement(descriptor, 3, UInt.serializer())
                    4 -> majorOperatingSystemVersion = decodeSerializableElement(descriptor, 4, UShort.serializer())
                    5 -> minorOperatingSystemVersion = decodeSerializableElement(descriptor, 5, UShort.serializer())
                    6 -> majorImageVersion = decodeSerializableElement(descriptor, 6, UShort.serializer())
                    7 -> minorImageVersion = decodeSerializableElement(descriptor, 7, UShort.serializer())
                    8 -> majorSubsystemVersion = decodeSerializableElement(descriptor, 8, UShort.serializer())
                    9 -> minorSubsystemVersion = decodeSerializableElement(descriptor, 9, UShort.serializer())
                    10 -> win32VersionValue = decodeSerializableElement(descriptor, 10, UInt.serializer())
                    11 -> sizeOfImage = decodeSerializableElement(descriptor, 11, UInt.serializer())
                    12 -> sizeOfHeaders = decodeSerializableElement(descriptor, 12, UInt.serializer())
                    13 -> checkSum = decodeSerializableElement(descriptor, 13, UInt.serializer())
                    14 -> subsystem = decodeSerializableElement(descriptor, 14, WindowsSubsystemsSerializer)
                    15 -> dllCharacteristics = decodeSerializableElement(descriptor, 15, DllCharacteristicsSerializer)
                    16 -> sizeOfStackReserve = decodeSerializableElement(descriptor, 16, ULong.serializer())
                    17 -> sizeOfStackCommit = decodeSerializableElement(descriptor, 17, ULong.serializer())
                    18 -> sizeOfHeapReserve = decodeSerializableElement(descriptor, 18, ULong.serializer())
                    19 -> sizeOfHeapCommit = decodeSerializableElement(descriptor, 19, ULong.serializer())
                    20 -> loaderFlags = decodeSerializableElement(descriptor, 20, UInt.serializer())
                    21 -> numbersOfRvaAndSizes = decodeIntElement(descriptor, 21)
                    22 -> exportTable = decodeSerializableElement(descriptor, 22, DataDirectoryItem.serializer())
                    23 -> importTable = decodeSerializableElement(descriptor, 23, DataDirectoryItem.serializer())
                    24 -> resourceTable = decodeSerializableElement(descriptor, 24, DataDirectoryItem.serializer())
                    25 -> exceptionTable = decodeSerializableElement(descriptor, 25, DataDirectoryItem.serializer())
                    26 -> certificateTable = decodeSerializableElement(descriptor, 26, DataDirectoryItem.serializer())
                    27 -> baseRelocationTable =
                        decodeSerializableElement(descriptor, 27, DataDirectoryItem.serializer())

                    28 -> debug = decodeSerializableElement(descriptor, 28, DataDirectoryItem.serializer())
                    29 -> architecture = decodeSerializableElement(descriptor, 29, DataDirectoryItem.serializer())
                    30 -> globalPtr = decodeSerializableElement(descriptor, 30, DataDirectoryItem.serializer())
                    31 -> tlsTable = decodeSerializableElement(descriptor, 31, DataDirectoryItem.serializer())
                    32 -> loadConfigTable = decodeSerializableElement(descriptor, 32, DataDirectoryItem.serializer())
                    33 -> boundImport = decodeSerializableElement(descriptor, 33, DataDirectoryItem.serializer())
                    34 -> iat = decodeSerializableElement(descriptor, 34, DataDirectoryItem.serializer())
                    35 -> delayImportDescriptor =
                        decodeSerializableElement(descriptor, 35, DataDirectoryItem.serializer())

                    36 -> clrRuntimeHeader = decodeSerializableElement(descriptor, 36, DataDirectoryItem.serializer())
                    else -> error("Unexpected index: $index")
                }
            }

            requireNotNull(magic) { "magic is required" }
            requireNotNull(imageBase) { "imageBase is required" }
            requireNotNull(subsystem) { "subsystem is required" }

            WindowsSpecifiedHeader(
                magic = magic,
                imageBase = imageBase,
                sectionAlignment = sectionAlignment,
                fileAlignment = fileAlignment,
                majorOperatingSystemVersion = majorOperatingSystemVersion,
                minorOperatingSystemVersion = minorOperatingSystemVersion,
                majorImageVersion = majorImageVersion,
                minorImageVersion = minorImageVersion,
                majorSubsystemVersion = majorSubsystemVersion,
                minorSubsystemVersion = minorSubsystemVersion,
                win32VersionValue = win32VersionValue,
                sizeOfImage = sizeOfImage,
                sizeOfHeaders = sizeOfHeaders,
                checkSum = checkSum,
                subsystem = subsystem,
                dllCharacteristics = dllCharacteristics,
                sizeOfStackReserve = sizeOfStackReserve,
                sizeOfStackCommit = sizeOfStackCommit,
                sizeOfHeapReserve = sizeOfHeapReserve,
                sizeOfHeapCommit = sizeOfHeapCommit,
                loaderFlags = loaderFlags,
                numbersOfRvaAndSizes = numbersOfRvaAndSizes,
                exportTable = exportTable,
                importTable = importTable,
                resourceTable = resourceTable,
                exceptionTable = exceptionTable,
                certificateTable = certificateTable,
                baseRelocationTable = baseRelocationTable,
                debug = debug,
                architecture = architecture,
                globalPtr = globalPtr,
                tlsTable = tlsTable,
                loadConfigTable = loadConfigTable,
                boundImport = boundImport,
                iat = iat,
                delayImportDescriptor = delayImportDescriptor,
                clrRuntimeHeader = clrRuntimeHeader
            )
        }
    }
} 