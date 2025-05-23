package space.iseki.executables.sbom

import space.iseki.executables.common.Address64
import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.OpenedFile
import space.iseki.executables.common.ReadableSectionContainer
import space.iseki.executables.common.VirtualMemoryReadable
import space.iseki.executables.common.readFully
import space.iseki.executables.elf.ElfFile
import space.iseki.executables.elf.ElfPFlags
import space.iseki.executables.elf.ElfPType
import space.iseki.executables.macho.MachoFile
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
class GoSBom internal constructor(
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
         * @return Go build information object
         * @throws SBomNotFoundException if SBOM data cannot be found in the file
         * @throws GoSBomReadingException if an error occurs during SBOM reading
         */
        fun readFrom(file: OpenedFile): GoSBom {
            try {
                if (file !is ReadableSectionContainer || file !is VirtualMemoryReadable) throw SBomNotFoundException("File is not supported")
                val vm = file.virtualMemory()
                val vAddr: Long
                val memSize: Long

                when (file) {
                    is ElfFile -> {
                        val section = file.sections.firstOrNull { it.name == ".go.buildinfo" }
                        if (section != null) {
                            vAddr = section.sectionHeader.shAddr.toLong()
                            memSize = section.sectionHeader.shSize.toLong()
                        } else {
                            val progHeader = file.programHeaders.firstOrNull {
                                it.pType == ElfPType.PT_LOAD && ElfPFlags.PF_W in it.pFlags && ElfPFlags.PF_X !in it.pFlags
                            }
                            if (progHeader != null) {
                                vAddr = progHeader.pVaddr.toLong()
                                memSize = progHeader.pMemsz.toLong()
                            } else {
                                throw SBomNotFoundException("Cannot find data section containing Go buildinfo")
                            }
                        }
                    }

                    is PEFile -> {
                        val section = file.sections.firstOrNull {
                            val f =
                                it.characteristics.value and SectionFlags.Constants.IMAGE_SCN_ALIGN_32BYTES.inv() == SectionFlags.Constants.IMAGE_SCN_MEM_READ or SectionFlags.Constants.IMAGE_SCN_MEM_WRITE or SectionFlags.Constants.IMAGE_SCN_CNT_INITIALIZED_DATA
                            it.virtualAddress.value != 0u && it.size > 0 && f
                        } ?: throw SBomNotFoundException("Cannot find data section containing Go buildinfo")
                        vAddr = section.virtualAddress.value.toULong().toLong()
                        memSize = section.virtualSize.toLong()
                    }

                    is MachoFile -> {
                        val se = file.sections.firstOrNull { it.name == "__go_buildinfo" }
                        if (se != null) {
                            vAddr = se.header.addr.toLong()
                            memSize = se.size
                        } else {
                            throw SBomNotFoundException("Cannot find data section containing Go buildinfo")
                        }
                    }
                    else -> throw SBomNotFoundException("Unsupported file format")
                }

                // Search for magic number
                val magicAddr = searchMagic(vm, vAddr, memSize)

                // Read the header
                val header = ByteArray(HEADER_SIZE)
                vm.readFully(magicAddr, header)

                // Parse flags
                val flags = header[FLAG_OFFSET]

                val version: String
                var moduleInfo: String

                if ((flags and FLAGS_VERSION_MASK.toByte()) == FLAGS_VERSION_INL.toByte()) {
                    // Go 1.18+ uses inline strings
                    val (vers, nAddr) = decodeString(vm, magicAddr + HEADER_SIZE)
                    val (mod, _) = decodeString(vm, nAddr)
                    version = vers
                    moduleInfo = mod
                } else {
                    // Go 1.17 及更早版本：version/mod 是指针形式
                    val ptrSize = header[PTR_SIZE_OFFSET].toInt()
                    val flagsByte = flags.toInt()
                    val bigEndian = (flagsByte and FLAGS_ENDIAN_MASK) == FLAGS_ENDIAN_BIG
                    val versPtrOffset = VERS_PTR_OFFSET
                    val modPtrOffset = VERS_PTR_OFFSET + ptrSize

                    val versPtr = readPtr(header, versPtrOffset, ptrSize, bigEndian)
                    val modPtr = readPtr(header, modPtrOffset, ptrSize, bigEndian)

                    version = readGoString(vm, versPtr, ptrSize, bigEndian)
                    moduleInfo = readGoString(vm, modPtr, ptrSize, bigEndian)
                }
                // Parse module info
                var parsedBuildInfo: GoBuildInfo? = null

                if (moduleInfo.isNotEmpty()) {
                    // Process module info: remove delimiters
                    if (moduleInfo.length >= 33 && moduleInfo[moduleInfo.length - 17] == '\n') {
                        moduleInfo = moduleInfo.substring(16, moduleInfo.length - 16)
                        // Try to parse build info
                        try {
                            parsedBuildInfo = GoBuildInfo.parse(moduleInfo)
                            // Add version information as it's not in the build info
                            parsedBuildInfo = parsedBuildInfo.copy(goVersion = version)
                        } catch (e: Exception) {
                            throw GoSBomReadingException("Failed to parse build info", e)
                        }
                    } else {
                        moduleInfo = ""
                    }
                }

                return GoSBom(version, moduleInfo, parsedBuildInfo)
            } catch (e: SBomReadingException) {
                // Re-throw SBOM exceptions
                throw e
            } catch (e: Exception) {
                // Wrap other exceptions
                throw GoSBomReadingException("Error reading Go SBOM", e)
            }
        }

        /**
         * Tries to read Go build information from a file, returning null if any error occurs.
         * This is a convenience method that catches all exceptions.
         *
         * @param file The opened file
         * @return Go build information object, or null if not found or on error
         */
        fun readFromOrNull(file: OpenedFile): GoSBom? {
            return try {
                readFrom(file)
            } catch (e: Exception) {
                null
            }
        }

        private fun searchMagic(da: DataAccessor, start: Long, size: Long): Address64 {
            if (size <= 0) throw SBomNotFoundException("Go buildinfo magic number not found")

            val end = start + size
            var currentStart = ((start + ALIGN - 1) / ALIGN) * ALIGN.toLong()

            val buffer = ByteArray(SEARCH_CHUNK_SIZE)

            while (currentStart < end) {
                val remaining = end - currentStart
                val chunkSize = minOf(SEARCH_CHUNK_SIZE.toLong(), remaining).toInt()

                val n = da.readAtMost(currentStart, buffer)
                if (n <= 0) break

                val data = if (n < buffer.size) buffer.copyOf(n) else buffer

                var index = 0
                while (index < data.size) {
                    val magicIndex = findByteSequence(data, MAGIC, index)
                    if (magicIndex < 0) break

                    val absolutePos = currentStart + magicIndex

                    if (absolutePos + HEADER_SIZE > end) {
                        throw SBomNotFoundException("Go buildinfo header incomplete")
                    }

                    if (absolutePos % ALIGN.toLong() == 0L) {
                        return Address64(absolutePos.toULong())
                    }

                    index = ((magicIndex + ALIGN) / ALIGN) * ALIGN
                }

                currentStart += chunkSize
            }

            throw SBomNotFoundException("Go buildinfo magic number not found")
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

        private fun decodeString(da: DataAccessor, addr: Address64): Pair<String, Address64> {
            val buf = ByteArray(10)
            da.readFully(addr, buf)
            val (len, n) = uvarint(buf)
            if (n <= 0) {
                throw GoSBomReadingException("Failed to decode string length, addr: $addr")
            }
            val nAddr = addr + n.toULong()
            val sBuf = ByteArray(len.toInt())
            da.readFully(nAddr, sBuf)
            val str = sBuf.decodeToString()
            return str to nAddr + len
        }

        private fun uvarint(buf: ByteArray): Pair<ULong, Int> {
            var x = 0UL
            var s = 0
            for ((i, b) in buf.withIndex()) {
                if (i == 10) {
                    return 0UL to -(i + 1)
                }
                val ub = b.toInt() and 0xFF
                if (ub < 0x80) {
                    if (i == 10 - 1 && ub > 1) {
                        return 0UL to -(i + 1)
                    }
                    return (x or (ub.toULong() shl s)) to (i + 1)
                }
                x = x or ((ub and 0x7F).toULong() shl s)
                s += 7
            }
            return 0UL to 0
        }

        private fun readGoString(vm: DataAccessor, addr: Long, ptrSize: Int, bigEndian: Boolean): String {
            val headerSize = ptrSize * 2

            val headerAndData = ByteArray(headerSize)
            vm.readFully(addr, headerAndData)

            val dataAddr = readPtr(headerAndData, 0, ptrSize, bigEndian)
            val dataLen = readPtr(headerAndData, ptrSize, ptrSize, bigEndian)

            val buffer = ByteArray(dataLen.toInt())
            vm.readFully(dataAddr, buffer)
            return buffer.decodeToString()
        }


        // Read pointer value
        private fun readPtr(buffer: ByteArray, offset: Int, ptrSize: Int, bigEndian: Boolean): Long {
            return when (ptrSize) {
                4 -> if (bigEndian) buffer.u4b(offset).toLong() else buffer.u4l(offset).toLong()
                8 -> if (bigEndian) buffer.u8b(offset).toLong() else buffer.u8l(offset).toLong()
                else -> throw GoSBomReadingException("Invalid pointer size: $ptrSize")
            }
        }

    }
}