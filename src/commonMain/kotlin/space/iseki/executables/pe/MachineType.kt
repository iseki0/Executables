package space.iseki.executables.pe

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@Serializable(with = MachineType.Serializer::class)
@JvmInline
value class MachineType(val rawValue: Short) {
    object Serializer : KSerializer<MachineType> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): MachineType {
            try {
                return valueOf(decoder.decodeString())
            } catch (e: IllegalArgumentException) {
                throw SerializationException(e.message)
            }
        }

        override fun serialize(encoder: Encoder, value: MachineType) {
            encoder.encodeString(value.toString(false))
        }

    }

    object Constants {
        const val UNKNOWN = 0x0000.toShort()
        const val ALPHA = 0x0184.toShort()
        const val ALPHA64 = 0x0284.toShort()
        const val AM33 = 0x01d3.toShort()
        const val AMD64 = 0x8664.toShort()
        const val ARM = 0x01c0.toShort()
        const val ARM64 = 0xaa64.toShort()
        const val ARMNT = 0x01c4.toShort()
        const val EBC = 0x0ebc.toShort()
        const val I386 = 0x014c.toShort()
        const val IA64 = 0x0200.toShort()
        const val LOONGARCH32 = 0x6232.toShort()
        const val LOONGARCH64 = 0x6264.toShort()
        const val M32R = 0x9041.toShort()
        const val MIPS16 = 0x0266.toShort()
        const val MIPSFPU = 0x0366.toShort()
        const val MIPSFPU16 = 0x0466.toShort()
        const val POWERPC = 0x01f0.toShort()
        const val POWERPCFP = 0x01f1.toShort()
        const val R4000 = 0x0166.toShort()
        const val RISCV32 = 0x5032.toShort()
        const val RISCV64 = 0x5064.toShort()
        const val RISCV128 = 0x5128.toShort()
        const val SH3 = 0x01a2.toShort()
        const val SH3DSP = 0x01a3.toShort()
        const val SH4 = 0x01a6.toShort()
        const val SH5 = 0x01a8.toShort()
        const val THUMB = 0x01c2.toShort()
        const val WCEMIPSV2 = 0x0169.toShort()
    }

    override fun toString(): String = toString(true)

    private fun toString(wrap: Boolean): String = when (rawValue) {
        Constants.UNKNOWN -> "UNKNOWN"
        Constants.ALPHA -> "ALPHA"
        Constants.ALPHA64 -> "ALPHA64"
        Constants.AM33 -> "AM33"
        Constants.AMD64 -> "AMD64"
        Constants.ARM -> "ARM"
        Constants.ARM64 -> "ARM64"
        Constants.ARMNT -> "ARMNT"
        Constants.EBC -> "EBC"
        Constants.I386 -> "I386"
        Constants.IA64 -> "IA64"
        Constants.LOONGARCH32 -> "LOONGARCH32"
        Constants.LOONGARCH64 -> "LOONGARCH64"
        Constants.M32R -> "M32R"
        Constants.MIPS16 -> "MIPS16"
        Constants.MIPSFPU -> "MIPSFPU"
        Constants.MIPSFPU16 -> "MIPSFPU16"
        Constants.POWERPC -> "POWERPC"
        Constants.POWERPCFP -> "POWERPCFP"
        Constants.R4000 -> "R4000"
        Constants.RISCV32 -> "RISCV32"
        Constants.RISCV64 -> "RISCV64"
        Constants.RISCV128 -> "RISCV128"
        Constants.SH3 -> "SH3"
        Constants.SH3DSP -> "SH3DSP"
        Constants.SH4 -> "SH4"
        Constants.SH5 -> "SH5"
        Constants.THUMB -> "THUMB"
        Constants.WCEMIPSV2 -> "WCEMIPSV2"
        else -> if (wrap) "MachineType(0x${toHexString()})" else toHexString()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun toHexString() = rawValue.toHexString()

    companion object {
        val UNKNOWN = MachineType(Constants.UNKNOWN)
        val ALPHA = MachineType(Constants.ALPHA)
        val ALPHA64 = MachineType(Constants.ALPHA64)
        val AM33 = MachineType(Constants.AM33)
        val AMD64 = MachineType(Constants.AMD64)
        val ARM = MachineType(Constants.ARM)
        val ARM64 = MachineType(Constants.ARM64)
        val ARMNT = MachineType(Constants.ARMNT)
        val EBC = MachineType(Constants.EBC)
        val I386 = MachineType(Constants.I386)
        val IA64 = MachineType(Constants.IA64)
        val LOONGARCH32 = MachineType(Constants.LOONGARCH32)
        val LOONGARCH64 = MachineType(Constants.LOONGARCH64)
        val M32R = MachineType(Constants.M32R)
        val MIPS16 = MachineType(Constants.MIPS16)
        val MIPSFPU = MachineType(Constants.MIPSFPU)
        val MIPSFPU16 = MachineType(Constants.MIPSFPU16)
        val POWERPC = MachineType(Constants.POWERPC)
        val POWERPCFP = MachineType(Constants.POWERPCFP)
        val R4000 = MachineType(Constants.R4000)
        val RISCV32 = MachineType(Constants.RISCV32)
        val RISCV64 = MachineType(Constants.RISCV64)
        val RISCV128 = MachineType(Constants.RISCV128)
        val SH3 = MachineType(Constants.SH3)
        val SH3DSP = MachineType(Constants.SH3DSP)
        val SH4 = MachineType(Constants.SH4)
        val SH5 = MachineType(Constants.SH5)
        val THUMB = MachineType(Constants.THUMB)
        val WCEMIPSV2 = MachineType(Constants.WCEMIPSV2)

        @JvmStatic
        fun toString(rawValue: Short): String {
            return MachineType(rawValue).toString()
        }

        @JvmStatic
        fun valueOfOrNull(s: String): MachineType? = when (s) {
            "UNKNOWN" -> UNKNOWN
            "ALPHA" -> ALPHA
            "ALPHA64" -> ALPHA64
            "AM33" -> AM33
            "AMD64" -> AMD64
            "ARM" -> ARM
            "ARM64" -> ARM64
            "ARMNT" -> ARMNT
            "EBC" -> EBC
            "I386" -> I386
            "IA64" -> IA64
            "LOONGARCH32" -> LOONGARCH32
            "LOONGARCH64" -> LOONGARCH64
            "M32R" -> M32R
            "MIPS16" -> MIPS16
            "MIPSFPU" -> MIPSFPU
            "MIPSFPU16" -> MIPSFPU16
            "POWERPC" -> POWERPC
            "POWERPCFP" -> POWERPCFP
            "R4000" -> R4000
            "RISCV32" -> RISCV32
            "RISCV64" -> RISCV64
            "RISCV128" -> RISCV128
            "SH3" -> SH3
            "SH3DSP" -> SH3DSP
            "SH4" -> SH4
            "SH5" -> SH5
            "THUMB" -> THUMB
            "WCEMIPSV2" -> WCEMIPSV2
            else -> null
        }

        @JvmStatic
        fun valueOf(s: String): MachineType {
            valueOfOrNull(s)?.also { return it }
            val v = try {
                when {
                    s.startsWith("MachineType(0x") -> s.removePrefix("MachineType(0x").removeSuffix(")").toShort(16)
                    s.startsWith("0x") -> s.removePrefix("0x").toShort(16)
                    else -> s.toShort()
                }
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("MachineType should be a hex number or a valid name, but got $s")
            }
            return MachineType(v)
        }
    }
}


