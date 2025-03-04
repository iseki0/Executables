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
 * Represents CPU information in a Mach-O file, including CPU type and subtype.
 *
 * Reference:
 * [machine.h](https://github.com/opensource-apple/cctools/blob/fdb4825f303fd5c0751be524babd32958181b3ed/include/mach/machine.h)
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

            val subtype = when (dto.type.value and 0xFEFFFFFFu.toInt()) {
                CpuType.Constants.ARM -> ArmCpuSubtype.valueOf(dto.subtype).value
                CpuType.Constants.X86 -> X86CpuSubtype.valueOf(dto.subtype).value
                CpuType.Constants.POWERPC -> PowerPcCpuSubtype.valueOf(dto.subtype).value
                CpuType.Constants.MC680X0 -> MC680X0CpuSubtype.valueOf(dto.subtype).value
                CpuType.Constants.MIPS -> MipsCpuSubtype.valueOf(dto.subtype).value
                CpuType.Constants.SPARC -> SparcCpuSubtype.valueOf(dto.subtype).value
                CpuType.Constants.HP_PA -> HpPaCpuSubtype.valueOf(dto.subtype).value
                CpuType.Constants.VAX -> VaxCpuSubtype.valueOf(dto.subtype).value
                else -> return parseSubtypeHex(dto.type, dto.subtype)
            }
            return valueOf(dto.type, subtype.toUInt())

        }

        private fun parseSubtypeHex(type: CpuType, subtypeStr: String): CpuInfo {
            val subtype = subtypeStr.removePrefix("0x").toUIntOrNull(16)
                ?: throw IllegalArgumentException("Unknown CPU subtype: $subtypeStr")
            return valueOf(type, subtype)
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
     * Determines if this CPU type is 64-bit.
     * If the 0x01000000 bit is set in the CPU type, it indicates a 64-bit version of the architecture.
     */
    val is64Bit: Boolean
        get() = (cpuType.value and 0x01000000) != 0

    /**
     * Gets the base CPU type (with the 64-bit flag removed).
     */
    val baseCpuType: CpuType
        get() = CpuType(cpuType.value.toUInt() and 0xFEFFFFFFu)

    @OptIn(ExperimentalStdlibApi::class)
    private val realTypeString: String
        get() = when (baseCpuType.value) {
            CpuType.Constants.ARM -> ArmCpuSubtype(cpuSubtype).toString()
            CpuType.Constants.X86 -> X86CpuSubtype(cpuSubtype).toString()
            CpuType.Constants.POWERPC -> PowerPcCpuSubtype(cpuSubtype).toString()
            CpuType.Constants.MC680X0 -> MC680X0CpuSubtype(cpuSubtype).toString()
            CpuType.Constants.MIPS -> MipsCpuSubtype(cpuSubtype).toString()
            CpuType.Constants.SPARC -> SparcCpuSubtype(cpuSubtype).toString()
            CpuType.Constants.HP_PA -> HpPaCpuSubtype(cpuSubtype).toString()
            CpuType.Constants.VAX -> VaxCpuSubtype(cpuSubtype).toString()
            else -> "0x" + cpuSubtype.toHexString()
        }


    override fun toString(): String {
        return "CpuInfo(type=$cpuType, subtype=$realTypeString)"
    }

    companion object {
        /**
         * Creates a CpuInfo instance from CPU type and subtype.
         *
         * @param cpuType The CPU type
         * @param cpuSubtype The CPU subtype
         * @return A new CpuInfo instance
         */
        fun valueOf(cpuType: CpuType, cpuSubtype: UInt): CpuInfo {
            val value = (cpuType.value.toULong() shl 32) or cpuSubtype.toULong()
            return CpuInfo(value)
        }

    }
} 