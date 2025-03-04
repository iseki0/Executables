package space.iseki.executables.macho

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import kotlin.jvm.JvmInline

/**
 * 表示Mach-O文件中的CPU信息，包含CPU类型和子类型
 *
 */
@JvmInline
@Serializable(with = CpuInfo.Serializer::class)
value class CpuInfo(val value: ULong) {
    object Serializer : KSerializer<CpuInfo> {

        @Serializable
        private data class Dto(val type: CpuType, val subtype: String)

        override val descriptor: SerialDescriptor
            get() = serialDescriptor<Dto>()

        override fun deserialize(decoder: Decoder): CpuInfo {
            val dto = decoder.decodeSerializableValue(Dto.serializer())
            when (dto.type) {
                CpuType.ARM -> {
                    val subtype = ArmCpuSubtype.valueOf(dto.subtype)
                    return valueOf(dto.type, subtype.value.toUInt())
                }

                CpuType.X86 -> {
                    val subtype = X86CpuSubtype.valueOf(dto.subtype)
                    return valueOf(dto.type, subtype.value.toUInt())
                }

                else -> {
                    val subtype = dto.subtype.toUIntOrNull()
                        ?: throw IllegalArgumentException("Unknown CPU subtype: ${dto.subtype}")
                    return valueOf(dto.type, subtype)
                }
            }
        }

        override fun serialize(encoder: Encoder, value: CpuInfo) {
            Dto(
                type = value.cpuType,
                subtype = value.realTypeString,
            ).also { encoder.encodeSerializableValue(serializer<Dto>(), it) }
        }

    }

    val cpuType: CpuType
        get() = CpuType((value shr 32).toUInt())
    val cpuSubtype: UInt
        get() = value.toUInt()

    /**
     * 判断是否为64位CPU类型
     * 如果CPU类型的0x01000000位被设置，则表示这是64位版本的指令集架构
     */
    val is64Bit: Boolean
        get() = (cpuType.value and 0x01000000) != 0

    /**
     * 获取基本CPU类型（移除64位标志位）
     */
    val baseCpuType: CpuType
        get() = CpuType(cpuType.value.toUInt() and 0xFEFFFFFFu)

    @OptIn(ExperimentalStdlibApi::class)
    private val realTypeString: String
        get() = when (baseCpuType) {
            CpuType.ARM -> ArmCpuSubtype(cpuSubtype).toString()
            CpuType.X86 -> X86CpuSubtype(cpuSubtype).toString()
            else -> "0x" + cpuSubtype.toHexString()
        }

    override fun toString(): String {
        return "CpuInfo(type=$cpuType, subtype=$realTypeString)"
    }

    companion object {
        /**
         * 从CPU类型和子类型创建CpuInfo
         *
         * @param cpuType CPU类型
         * @param cpuSubtype CPU子类型
         * @return 新的CpuInfo实例
         */
        fun valueOf(cpuType: CpuType, cpuSubtype: UInt): CpuInfo {
            val value = (cpuType.value.toULong() shl 32) or cpuSubtype.toULong()
            return CpuInfo(value)
        }

    }
} 