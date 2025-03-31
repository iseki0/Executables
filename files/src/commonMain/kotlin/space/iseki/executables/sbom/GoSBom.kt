package space.iseki.executables.sbom

import space.iseki.executables.common.OpenedFile
import space.iseki.executables.common.ReadableSection
import space.iseki.executables.common.ReadableSectionContainer
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.elf.ElfFile
import space.iseki.executables.pe.PEFile
import space.iseki.executables.pe.SectionFlags
import space.iseki.executables.share.u4b
import space.iseki.executables.share.u4l
import space.iseki.executables.share.u8b
import space.iseki.executables.share.u8l
import kotlin.experimental.and

/**
 * 用于从 Go 二进制中提取构建信息的类
 */
class GoSBom(
    private val version: String,
    private val moduleInfo: String,
) : ReadableStructure {
    override val fields: Map<String, Any>
        get() = mapOf(
            "version" to version, "moduleInfo" to moduleInfo
        )

    companion object {
        // Go buildinfo 魔数
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

        // header 大小常量
        private const val HEADER_SIZE = 32
        private const val ALIGN = 16

        // flag 掩码和偏移量常量
        private const val FLAG_OFFSET = 15
        private const val PTR_SIZE_OFFSET = 14
        private const val VERS_PTR_OFFSET = 16

        private const val FLAGS_ENDIAN_MASK = 0x1
        private const val FLAGS_ENDIAN_BIG = 0x1

        private const val FLAGS_VERSION_MASK = 0x2
        private const val FLAGS_VERSION_INL = 0x2

        private const val SEARCH_CHUNK_SIZE = 1 * 1024 * 1024 // 1MB

        /**
         * 从文件中读取 Go 构建信息
         *
         * @param file 已打开的文件
         * @return Go 构建信息对象，如果未找到则返回 null
         */
        fun open(file: OpenedFile): GoSBom? {
            try {
                if (file !is ReadableSectionContainer) return null

                // 查找包含构建信息的节
                val dataSection = when (file) {
                    is PEFile -> findPeDataSection(file)
                    is ElfFile -> findElfDataSection(file)
                    else -> return null
                } ?: return null

                // 搜索魔数
                val magicAddr = searchMagic(dataSection) ?: return null

                // 读取头部
                val header = ByteArray(HEADER_SIZE)
                dataSection.readBytes(magicAddr, header, 0, HEADER_SIZE)

                // 解析标志
                val flags = header[FLAG_OFFSET]

                var version = ""
                var moduleInfo = ""

                if ((flags and FLAGS_VERSION_MASK.toByte()) == FLAGS_VERSION_INL.toByte()) {
                    // 1.18+ 版本使用内联字符串
                    val result = decodeInlineStrings(dataSection, magicAddr + HEADER_SIZE)
                    version = result?.first ?: return null
                    moduleInfo = result.second
                } else {
                    // 旧版本使用指针
                    val ptrSize = header[PTR_SIZE_OFFSET].toInt()
                    val isBigEndian = (flags and FLAGS_ENDIAN_MASK.toByte()) == FLAGS_ENDIAN_BIG.toByte()

                    // 读取版本和模块信息的指针
                    val verPtr = readPtr(header, VERS_PTR_OFFSET, ptrSize, isBigEndian)
                    val modPtr = readPtr(header, VERS_PTR_OFFSET + ptrSize, ptrSize, isBigEndian)

                    // 读取字符串
                    version = readGoString(dataSection, verPtr, ptrSize, isBigEndian) ?: return null
                    moduleInfo = readGoString(dataSection, modPtr, ptrSize, isBigEndian) ?: ""
                }

                // 处理模块信息：去除分隔符
                if (moduleInfo.length >= 33 && moduleInfo[moduleInfo.length - 17] == '\n') {
                    moduleInfo = moduleInfo.substring(16, moduleInfo.length - 16)
                } else {
                    moduleInfo = ""
                }

                return GoSBom(version, moduleInfo)
            } catch (e: Exception) {
                // 捕获所有异常
                return null
            }
        }

        private fun findPeDataSection(file: PEFile): ReadableSection? {
            return file.sections.asSequence()
                .filter { it.virtualAddress.value != 0u }
                .filter { it.size > 0 }
                .filter { (SectionFlags.IMAGE_SCN_CNT_INITIALIZED_DATA + SectionFlags.IMAGE_SCN_MEM_READ + SectionFlags.IMAGE_SCN_MEM_WRITE) in it.characteristics }
                .firstOrNull()
        }

        // 在 ELF 文件中寻找数据段
        private fun findElfDataSection(file: ElfFile): ReadableSection? {
            return file.sections.firstOrNull { section -> section.name == ".go.buildinfo" }
        }

        // 搜索 Go buildinfo 魔数
        private fun searchMagic(section: ReadableSection): Long? {
            val size = section.size
            if (size <= 0) return null

            // 计算对齐后的起始位置
            var currentStart = ((ALIGN - 1) / ALIGN) * ALIGN.toLong()

            val buffer = ByteArray(SEARCH_CHUNK_SIZE)

            while (currentStart < size) {
                val chunkSize = minOf(SEARCH_CHUNK_SIZE.toLong(), size - currentStart).toInt()

                section.readBytes(currentStart, buffer, 0, chunkSize)

                val data = buffer.copyOf(chunkSize)
                var index = 0

                while (index < data.size) {
                    // 查找魔数
                    val magicIndex = findByteSequence(data, MAGIC, index)
                    if (magicIndex < 0) break

                    // 检查是否有足够空间容纳完整头部
                    if (currentStart + magicIndex + HEADER_SIZE > size) {
                        return null
                    }

                    // 检查对齐
                    if (magicIndex % ALIGN == 0) {
                        // 找到了!
                        return currentStart + magicIndex
                    }

                    // 继续搜索，移动到下一个对齐位置
                    index = ((magicIndex + ALIGN) / ALIGN) * ALIGN
                    if (index >= data.size) break
                }

                currentStart += chunkSize
            }

            return null
        }

        // 查找字节序列
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

        // 解码内联字符串 (Go 1.18+)
        private fun decodeInlineStrings(section: ReadableSection, addr: Long): Pair<String, String>? {
            try {
                // 首先读取版本字符串
                val versionResult = decodeVarLenString(section, addr) ?: return null
                val version = versionResult.first
                val nextAddr = versionResult.second

                // 然后读取模块信息字符串
                val moduleResult = decodeVarLenString(section, nextAddr) ?: return version to ""

                return version to moduleResult.first
            } catch (e: Exception) {
                return null
            }
        }

        // 解码可变长度字符串
        private fun decodeVarLenString(section: ReadableSection, addr: Long): Pair<String, Long>? {
            try {
                val varintBuf = ByteArray(10) // 足够存储 64 位 varint
                section.readBytes(addr, varintBuf, 0, varintBuf.size)

                // 解析 varint 长度
                var length = 0L
                var shift = 0
                var index = 0

                while (index < varintBuf.size) {
                    val b = varintBuf[index].toInt() and 0xFF
                    index++

                    length = length or ((b and 0x7F).toLong() shl shift)
                    if (b and 0x80 == 0) break

                    shift += 7
                    if (shift > 63) return null // 溢出
                }

                if (length == 0L) return "" to addr + index

                // 读取字符串内容
                val stringBuf = ByteArray(length.toInt())
                section.readBytes(addr + index, stringBuf, 0, stringBuf.size)

                return stringBuf.decodeToString() to addr + index + length
            } catch (e: Exception) {
                return null
            }
        }

        // 读取指针值
        private fun readPtr(buffer: ByteArray, offset: Int, ptrSize: Int, bigEndian: Boolean): Long {
            return when (ptrSize) {
                4 -> if (bigEndian) buffer.u4b(offset).toLong() else buffer.u4l(offset).toLong()
                8 -> if (bigEndian) buffer.u8b(offset).toLong() else buffer.u8l(offset).toLong()
                else -> 0L
            }
        }

        // 读取 Go 字符串（旧版本格式）
        private fun readGoString(section: ReadableSection, addr: Long, ptrSize: Int, bigEndian: Boolean): String? {
            try {
                // 读取字符串头部（数据指针和长度）
                val hdrSize = 2 * ptrSize
                val hdr = ByteArray(hdrSize)
                section.readBytes(addr, hdr, 0, hdrSize)

                // 解析数据指针和长度
                val dataAddr = readPtr(hdr, 0, ptrSize, bigEndian)
                val dataLen = readPtr(hdr, ptrSize, ptrSize, bigEndian)

                if (dataLen == 0L) return ""

                // 读取字符串数据
                val data = ByteArray(dataLen.toInt())
                section.readBytes(dataAddr, data, 0, data.size)

                return data.decodeToString()
            } catch (e: Exception) {
                return null
            }
        }
    }
}