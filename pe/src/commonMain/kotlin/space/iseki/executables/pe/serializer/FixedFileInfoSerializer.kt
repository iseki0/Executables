package space.iseki.executables.pe.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import space.iseki.executables.pe.vi.FileInfoFlags
import space.iseki.executables.pe.vi.FileOs
import space.iseki.executables.pe.vi.FileType
import space.iseki.executables.pe.vi.FixedFileInfo
import space.iseki.executables.pe.vi.serializer.FileInfoFlagsSerializer
import space.iseki.executables.pe.vi.serializer.FileOsSerializer
import space.iseki.executables.pe.vi.serializer.FileTypeSerializer

internal object FixedFileInfoSerializer : KSerializer<FixedFileInfo> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("space.iseki.executables.pe.vi.FixedFileInfo") {
            element("structVersion", UInt.serializer().descriptor)
            element("fileVersion", FixedFileInfo.Version.serializer().descriptor)
            element("productVersion", FixedFileInfo.Version.serializer().descriptor)
            element("fileFlagsMask", UInt.serializer().descriptor)
            element("fileFlags", FileInfoFlagsSerializer.descriptor)
            element("fileOS", FileOsSerializer.descriptor)
            element("fileType", FileTypeSerializer.descriptor)
            element("fileSubtype", UInt.serializer().descriptor)
            element("fileDateMS", UInt.serializer().descriptor)
            element("fileDateLS", UInt.serializer().descriptor)
        }

    override fun serialize(encoder: Encoder, value: FixedFileInfo) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, UInt.serializer(), value.structVersion)
            encodeSerializableElement(descriptor, 1, FixedFileInfo.Version.serializer(), value.fileVersion)
            encodeSerializableElement(descriptor, 2, FixedFileInfo.Version.serializer(), value.productVersion)
            encodeSerializableElement(descriptor, 3, UInt.serializer(), value.fileFlagsMask)
            encodeSerializableElement(descriptor, 4, FileInfoFlagsSerializer, value.fileFlags)
            encodeSerializableElement(descriptor, 5, FileOsSerializer, value.fileOS)
            encodeSerializableElement(descriptor, 6, FileTypeSerializer, value.fileType)
            encodeSerializableElement(descriptor, 7, UInt.serializer(), value.fileSubtype)
            encodeSerializableElement(descriptor, 8, UInt.serializer(), value.fileDateMS)
            encodeSerializableElement(descriptor, 9, UInt.serializer(), value.fileDateLS)
        }
    }

    override fun deserialize(decoder: Decoder): FixedFileInfo {
        return decoder.decodeStructure(descriptor) {
            var structVersion: UInt? = null
            var fileVersion: FixedFileInfo.Version? = null
            var productVersion: FixedFileInfo.Version? = null
            var fileFlagsMask: UInt? = null
            var fileFlags: FileInfoFlags? = null
            var fileOS: FileOs? = null
            var fileType: FileType? = null
            var fileSubtype: UInt? = null
            var fileDateMS: UInt? = null
            var fileDateLS: UInt? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> structVersion = decodeSerializableElement(descriptor, 0, UInt.serializer())
                    1 -> fileVersion = decodeSerializableElement(descriptor, 1, FixedFileInfo.Version.serializer())
                    2 -> productVersion = decodeSerializableElement(descriptor, 2, FixedFileInfo.Version.serializer())
                    3 -> fileFlagsMask = decodeSerializableElement(descriptor, 5, UInt.serializer())
                    4 -> fileFlags = decodeSerializableElement(descriptor, 6, FileInfoFlagsSerializer)
                    5 -> fileOS = decodeSerializableElement(descriptor, 7, FileOsSerializer)
                    6 -> fileType = decodeSerializableElement(descriptor, 8, FileTypeSerializer)
                    7 -> fileSubtype = decodeSerializableElement(descriptor, 9, UInt.serializer())
                    8 -> fileDateMS = decodeSerializableElement(descriptor, 10, UInt.serializer())
                    9 -> fileDateLS = decodeSerializableElement(descriptor, 11, UInt.serializer())
                    else -> throw SerializationException("Unexpected index $index")
                }
            }

            FixedFileInfo(
                structVersion = structVersion ?: throw SerializationException("structVersion is required"),
                fileVersionMS = fileVersion?.ms ?: throw SerializationException("fileVersionMS is required"),
                fileVersionLS = fileVersion.ls,
                productVersionMS = productVersion?.ms ?: throw SerializationException("productVersionMS is required"),
                productVersionLS = productVersion.ls,
                fileFlagsMask = fileFlagsMask ?: throw SerializationException("fileFlagsMask is required"),
                fileFlags = fileFlags ?: throw SerializationException("fileFlags is required"),
                fileOS = fileOS ?: throw SerializationException("fileOS is required"),
                fileType = fileType ?: throw SerializationException("fileType is required"),
                fileSubtype = fileSubtype ?: throw SerializationException("fileSubtype is required"),
                fileDateMS = fileDateMS ?: throw SerializationException("fileDateMS is required"),
                fileDateLS = fileDateLS ?: throw SerializationException("fileDateLS is required")
            )
        }
    }
}