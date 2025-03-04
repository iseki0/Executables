package space.iseki.executables.macho

import kotlinx.serialization.Serializable
import space.iseki.executables.common.ReadableStructure
import space.iseki.executables.share.u4b
import space.iseki.executables.share.u4l

/**
 * 表示Mach-O文件的头部结构
 *
 * 对应C结构体：
 * ```c
 * struct mach_header {
 *     uint32_t    magic;      // mach magic number identifier
 *     cpu_type_t  cputype;    // cpu specifier
 *     cpu_subtype_t cpusubtype; // machine specifier
 *     uint32_t    filetype;   // type of file
 *     uint32_t    ncmds;      // number of load commands
 *     uint32_t    sizeofcmds; // the size of all the load commands
 *     uint32_t    flags;      // flags
 * };
 *
 * struct mach_header_64 {
 *     uint32_t    magic;      // mach magic number identifier
 *     cpu_type_t  cputype;    // cpu specifier
 *     cpu_subtype_t cpusubtype; // machine specifier
 *     uint32_t    filetype;   // type of file
 *     uint32_t    ncmds;      // number of load commands
 *     uint32_t    sizeofcmds; // the size of all the load commands
 *     uint32_t    flags;      // flags
 *     uint32_t    reserved;   // reserved
 * };
 * ```
 */
@Serializable
data class MachHeader(
    val magic: UInt,
    val cpuInfo: CpuInfo,
    val fileType: UInt,
    val numberOfCommands: UInt,
    val sizeOfCommands: UInt,
    val flags: UInt,
    val reserved: UInt = 0u,
) : ReadableStructure {

    /**
     * 判断是否为64位架构
     */
    val is64Bit: Boolean
        get() = magic == MH_MAGIC_64 || magic == MH_CIGAM_64

    /**
     * 判断是否需要字节序交换（大小端转换）
     */
    val needsSwap: Boolean
        get() = magic == MH_CIGAM || magic == MH_CIGAM_64

    /**
     * 获取结构体的所有字段
     */
    override val fields: Map<String, Any>
        get() = if (is64Bit) {
            mapOf(
                "magic" to "0x${magic.toString(16)}",
                "cpuInfo" to cpuInfo,
                "fileType" to "0x${fileType.toString(16)}",
                "numberOfCommands" to numberOfCommands,
                "sizeOfCommands" to sizeOfCommands,
                "flags" to "0x${flags.toString(16)}",
                "reserved" to reserved,
                "is64Bit" to is64Bit,
                "needsSwap" to needsSwap
            )
        } else {
            mapOf(
                "magic" to "0x${magic.toString(16)}",
                "cpuInfo" to cpuInfo,
                "fileType" to "0x${fileType.toString(16)}",
                "numberOfCommands" to numberOfCommands,
                "sizeOfCommands" to sizeOfCommands,
                "flags" to "0x${flags.toString(16)}",
                "is64Bit" to is64Bit,
                "needsSwap" to needsSwap
            )
        }

    override fun toString(): String {
        return fields.entries.joinToString("", "MachHeader(\n", "\n)") { (k, v) -> "   $k = $v,\n" }
    }

    companion object {
        /** 32位Mach-O头部魔数 */
        const val MH_MAGIC: UInt = 0xfeedfaceu

        /** 32位Mach-O头部魔数（字节序相反） */
        const val MH_CIGAM: UInt = 0xcefaedfeu

        /** 64位Mach-O头部魔数 */
        const val MH_MAGIC_64: UInt = 0xfeedfacfu

        /** 64位Mach-O头部魔数（字节序相反） */
        const val MH_CIGAM_64: UInt = 0xcffaedfeu

        /** 32位Mach-O头部大小（字节） */
        const val SIZE_32: Int = 28

        /** 64位Mach-O头部大小（字节） */
        const val SIZE_64: Int = 32

        /**
         * 从字节数组解析MachHeader
         *
         * @param bytes 包含Mach-O头部的字节数组
         * @param offset 头部在字节数组中的偏移量
         * @return 解析后的MachHeader对象
         * @throws IllegalArgumentException 如果魔数无效
         */
        fun parse(bytes: ByteArray, offset: Int = 0): MachHeader {
            // 读取魔数（始终使用大端序读取魔数）
            val magic = bytes.u4b(offset)

            // 检查魔数是否有效
            if (magic != MH_MAGIC && magic != MH_CIGAM && magic != MH_MAGIC_64 && magic != MH_CIGAM_64) {
                throw IllegalArgumentException("Invalid Mach-O magic number: 0x${magic.toString(16)}")
            }

            // 判断是否需要字节序交换
            val needsSwap = magic == MH_CIGAM || magic == MH_CIGAM_64

            // 判断是否为64位格式
            val is64Bit = magic == MH_MAGIC_64 || magic == MH_CIGAM_64

            // 根据字节序选择适当的读取函数
            val readUInt = if (needsSwap) bytes::u4l else bytes::u4b

            // 读取CPU类型和子类型
            val cpuType = readUInt(offset + 4)
            val cpuSubtype = readUInt(offset + 8)

            // 读取其他字段
            val fileType = readUInt(offset + 12)
            val numberOfCommands = readUInt(offset + 16)
            val sizeOfCommands = readUInt(offset + 20)
            val flags = readUInt(offset + 24)

            // 创建CpuInfo对象
            val cpuInfo = CpuInfo((cpuType.toULong() shl 32) or cpuSubtype.toULong())

            // 对于64位格式，读取reserved字段
            val reserved = if (is64Bit) {
                readUInt(offset + 28)
            } else {
                0u
            }

            return MachHeader(
                magic = magic,
                cpuInfo = cpuInfo,
                fileType = fileType,
                numberOfCommands = numberOfCommands,
                sizeOfCommands = sizeOfCommands,
                flags = flags,
                reserved = reserved
            )
        }
    }
} 