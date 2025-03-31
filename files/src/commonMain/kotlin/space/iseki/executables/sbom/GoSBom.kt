package space.iseki.executables.sbom

import space.iseki.executables.common.OpenedFile
import space.iseki.executables.common.ReadableSection
import space.iseki.executables.common.ReadableSectionContainer
import space.iseki.executables.elf.ElfFile
import space.iseki.executables.pe.PEFile
import space.iseki.executables.pe.SectionFlags
import space.iseki.executables.share.u4b
import space.iseki.executables.share.u4l
import space.iseki.executables.share.u8b
import space.iseki.executables.share.u8l
import kotlin.experimental.and

/**
 * Class for extracting build information from Go binaries.
 */
class GoSBom(
    private val version: String,
    private val moduleInfo: String,
    val buildInfo: GoBuildInfo? = null,
) {
    companion object {
        // Go buildinfo magic number
        private val MAGIC = byteArrayOf(
            0xFF.toByte(),
            ' '.code.toByte(),
            'G'.code.toByte(),
            'o'.code.toByte(),
            ' '.code.toByte(),
            'b'.code.toByte(),
            'u'.code.toByte(),
            'i'.code.toByte(),
            'l'.code.toByte(),
            'd'.code.toByte(),
            'i'.code.toByte(),
            'n'.code.toByte(),
            'f'.code.toByte(),
            ':'.code.toByte(),
        )

        // Header size constants
        private const val HEADER_SIZE = 32
        private const val ALIGN = 16

        // Flag masks and offset constants
        private const val FLAG_OFFSET = 15
        private const val PTR_SIZE_OFFSET = 14
        private const val VERS_PTR_OFFSET = 16

        private const val FLAGS_ENDIAN_MASK = 0x1
        private const val FLAGS_ENDIAN_BIG = 0x1

        private const val FLAGS_VERSION_MASK = 0x2
        private const val FLAGS_VERSION_INL = 0x2

        private const val SEARCH_CHUNK_SIZE = 1 * 1024 * 1024 // 1MB

        /**
         * Reads Go build information from a file.
         *
         * @param file The opened file
         * @return Go build information object, or null if not found
         */
        fun open(file: OpenedFile): GoSBom? {
            try {
                if (file !is ReadableSectionContainer) return null

                // Find section containing build information
                val dataSection = when (file) {
                    is PEFile -> findPeDataSection(file)
                    is ElfFile -> findElfDataSection(file)
                    else -> return null
                } ?: return null

                // Search for magic number
                val magicAddr = searchMagic(dataSection) ?: return null

                // Read the header
                val header = ByteArray(HEADER_SIZE)
                dataSection.readBytes(magicAddr, header, 0, HEADER_SIZE)

                // Parse flags
                val flags = header[FLAG_OFFSET]

                val version: String
                var moduleInfo: String

                if ((flags and FLAGS_VERSION_MASK.toByte()) == FLAGS_VERSION_INL.toByte()) {
                    // Go 1.18+ uses inline strings
                    val result = decodeInlineStrings(dataSection, magicAddr + HEADER_SIZE)
                    version = result?.first ?: return null
                    moduleInfo = result.second
                } else {
                    // Older versions use pointers
                    val ptrSize = header[PTR_SIZE_OFFSET].toInt()
                    val isBigEndian = (flags and FLAGS_ENDIAN_MASK.toByte()) == FLAGS_ENDIAN_BIG.toByte()

                    // Read version and module info pointers
                    val verPtr = readPtr(header, VERS_PTR_OFFSET, ptrSize, isBigEndian)
                    val modPtr = readPtr(header, VERS_PTR_OFFSET + ptrSize, ptrSize, isBigEndian)

                    // Read the strings
                    version = readGoString(dataSection, verPtr, ptrSize, isBigEndian) ?: return null
                    moduleInfo = readGoString(dataSection, modPtr, ptrSize, isBigEndian) ?: ""
                }

                // Parse module info
                var parsedBuildInfo: GoBuildInfo? = null

                if (moduleInfo.isNotEmpty()) {
                    // Process module info: remove delimiters
                    if (moduleInfo.length >= 33 && moduleInfo[moduleInfo.length - 17] == '\n') {
                        moduleInfo = moduleInfo.substring(16, moduleInfo.length - 16)
                        // Try to parse build info
                        try {
                            parsedBuildInfo = GoBuildInfo.parseBuildInfo(moduleInfo)
                            // Add version information as it's not in the build info
                            parsedBuildInfo = parsedBuildInfo.copy(goVersion = version)
                        } catch (e: Exception) {
                            // Parsing failed, ignore error
                        }
                    } else {
                        moduleInfo = ""
                    }
                }

                return GoSBom(version, moduleInfo, parsedBuildInfo)
            } catch (e: Exception) {
                // Catch all exceptions
                return null
            }
        }

        // Find data section in PE file
        private fun findPeDataSection(file: PEFile): ReadableSection? {
            return file.sections.asSequence()
                .filter { it.virtualAddress.value != 0u }
                .filter { it.size > 0 }
                .filter { (SectionFlags.IMAGE_SCN_CNT_INITIALIZED_DATA + SectionFlags.IMAGE_SCN_MEM_READ + SectionFlags.IMAGE_SCN_MEM_WRITE) in it.characteristics }
                .firstOrNull()
        }

        // Find data section in ELF file
        private fun findElfDataSection(file: ElfFile): ReadableSection? {
            return file.sections.firstOrNull { section -> section.name == ".go.buildinfo" }
        }

        // Search for Go buildinfo magic number
        private fun searchMagic(section: ReadableSection): Long? {
            val size = section.size
            if (size <= 0) return null

            // Calculate aligned start position
            var currentStart = ((ALIGN - 1) / ALIGN) * ALIGN.toLong()

            val buffer = ByteArray(SEARCH_CHUNK_SIZE)

            while (currentStart < size) {
                val chunkSize = minOf(SEARCH_CHUNK_SIZE.toLong(), size - currentStart).toInt()

                section.readBytes(currentStart, buffer, 0, chunkSize)

                val data = buffer.copyOf(chunkSize)
                var index = 0

                while (index < data.size) {
                    // Search for magic number
                    val magicIndex = findByteSequence(data, MAGIC, index)
                    if (magicIndex < 0) break

                    // Check if there's enough space for the complete header
                    if (currentStart + magicIndex + HEADER_SIZE > size) {
                        return null
                    }

                    // Check alignment
                    if (magicIndex % ALIGN == 0) {
                        // Found it!
                        return currentStart + magicIndex
                    }

                    // Continue searching, move to next aligned position
                    index = ((magicIndex + ALIGN) / ALIGN) * ALIGN
                    if (index >= data.size) break
                }

                currentStart += chunkSize
            }

            return null
        }

        // Find byte sequence
        private fun findByteSequence(data: ByteArray, sequence: ByteArray, startIndex: Int): Int {
            if (startIndex + sequence.size > data.size) return -1

            outer@ for (i in startIndex..data.size - sequence.size) {
                for (j in sequence.indices) {
                    if (data[i + j] != sequence[j]) {
                        continue@outer
                    }
                }
                return i
            }
            return -1
        }

        // Decode inline strings (Go 1.18+)
        private fun decodeInlineStrings(section: ReadableSection, addr: Long): Pair<String, String>? {
            try {
                // First read the version string
                val versionResult = decodeVarLenString(section, addr) ?: return null
                val version = versionResult.first
                val nextAddr = versionResult.second

                // Then read the module info string
                val moduleResult = decodeVarLenString(section, nextAddr) ?: return version to ""

                return version to moduleResult.first
            } catch (e: Exception) {
                return null
            }
        }

        // Decode variable length string
        private fun decodeVarLenString(section: ReadableSection, addr: Long): Pair<String, Long>? {
            try {
                val varintBuf = ByteArray(10) // Large enough for 64-bit varint
                section.readBytes(addr, varintBuf, 0, varintBuf.size)

                // Parse varint length
                var length = 0L
                var shift = 0
                var index = 0

                while (index < varintBuf.size) {
                    val b = varintBuf[index].toInt() and 0xFF
                    index++

                    length = length or ((b and 0x7F).toLong() shl shift)
                    if (b and 0x80 == 0) break

                    shift += 7
                    if (shift > 63) return null // Overflow
                }

                if (length == 0L) return "" to addr + index

                // Read string content
                val stringBuf = ByteArray(length.toInt())
                section.readBytes(addr + index, stringBuf, 0, stringBuf.size)

                return stringBuf.decodeToString() to addr + index + length
            } catch (e: Exception) {
                return null
            }
        }

        // Read pointer value
        private fun readPtr(buffer: ByteArray, offset: Int, ptrSize: Int, bigEndian: Boolean): Long {
            return when (ptrSize) {
                4 -> if (bigEndian) buffer.u4b(offset).toLong() else buffer.u4l(offset).toLong()
                8 -> if (bigEndian) buffer.u8b(offset).toLong() else buffer.u8l(offset).toLong()
                else -> 0L
            }
        }

        // Read Go string (older format)
        private fun readGoString(section: ReadableSection, addr: Long, ptrSize: Int, bigEndian: Boolean): String? {
            try {
                // Read string header (data pointer and length)
                val hdrSize = 2 * ptrSize
                val hdr = ByteArray(hdrSize)
                section.readBytes(addr, hdr, 0, hdrSize)

                // Parse data pointer and length
                val dataAddr = readPtr(hdr, 0, ptrSize, bigEndian)
                val dataLen = readPtr(hdr, ptrSize, ptrSize, bigEndian)

                if (dataLen == 0L) return ""

                // Read string data
                val data = ByteArray(dataLen.toInt())
                section.readBytes(dataAddr, data, 0, data.size)

                return data.decodeToString()
            } catch (e: Exception) {
                return null
            }
        }
    }
}