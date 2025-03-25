package space.iseki.executables.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

/**
 * Represents the type of executable file.
 */
@JvmInline
@Serializable(ExecutableFileType.Serializer::class)
value class ExecutableFileType(private val i: Byte) {
    internal object Serializer : KSerializer<ExecutableFileType> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<String>()

        override fun deserialize(decoder: Decoder): ExecutableFileType {
            val s = decoder.decodeString()
            try {
                return fromString(s)
            } catch (e: IllegalArgumentException) {
                throw SerializationException("Unknown executable file type: $s")
            }
        }

        override fun serialize(encoder: Encoder, value: ExecutableFileType) {
            encoder.encodeString(value.toString())
        }

    }

    companion object {
        val ELF = ExecutableFileType(1)
        val MACHO = ExecutableFileType(2)
        val PE = ExecutableFileType(3)

        /**
         * Returns the executable file type from the given string.
         * @param s the string representation of the executable file type
         * @return the executable file type
         * @throws IllegalArgumentException if the executable file type is unknown
         */
        @JvmStatic
        fun fromString(s: String): ExecutableFileType = when (s) {
            "ELF" -> ELF
            "Mach-O" -> MACHO
            "PE" -> PE
            else -> throw IllegalArgumentException("Unknown executable file type: $s")
        }

        /**
         * Returns the string representation of the given executable file type.
         * @param i the executable file type
         * @return the string representation of the given executable file type
         * @throws IllegalArgumentException if the executable file type is unknown
         */
        @JvmStatic
        fun toString(i: Byte): String = ExecutableFileType(i).toString()

        /**
         * Detects the type of the executable file from the provided [DataAccessor].
         *
         * @param dataAccessor the data accessor to read the file header.
         * @throws IOException if an I/O error occurs
         * @return the detected [ExecutableFileType], or `null` if the type could not be detected
         */
        @JvmStatic
        fun detect(dataAccessor: DataAccessor): ExecutableFileType? {
            val buf = ByteArray(4)
            val read = dataAccessor.readAtMost(0, buf, 0, 4)
            if (read < 4) {
                return null
            }
            return when {
                buf[0] == 0x4d.toByte() && buf[1] == 0x5a.toByte() -> PE
                buf[0] == 0x7f.toByte() && buf[1] == 0x45.toByte() && buf[2] == 0x4c.toByte() && buf[3] == 0x46.toByte() -> ELF

                // 32-bit little endian
                buf[0] == 0xfe.toByte() && buf[1] == 0xed.toByte() && buf[2] == 0xfa.toByte() && buf[3] == 0xce.toByte() -> MACHO
                // 32-bit big endian
                buf[0] == 0xce.toByte() && buf[1] == 0xfa.toByte() && buf[2] == 0xed.toByte() && buf[3] == 0xfe.toByte() -> MACHO
                // 64-bit little endian
                buf[0] == 0xfe.toByte() && buf[1] == 0xed.toByte() && buf[2] == 0xfa.toByte() && buf[3] == 0xcf.toByte() -> MACHO
                // 64-bit big endian
                buf[0] == 0xcf.toByte() && buf[1] == 0xfa.toByte() && buf[2] == 0xed.toByte() && buf[3] == 0xfe.toByte() -> MACHO
                // Fat binary (universal)
                buf[0] == 0xca.toByte() && buf[1] == 0xfe.toByte() && buf[2] == 0xba.toByte() && buf[3] == 0xbe.toByte() -> MACHO
                // Fat binary (reverse endian)
                buf[0] == 0xbe.toByte() && buf[1] == 0xba.toByte() && buf[2] == 0xfe.toByte() && buf[3] == 0xca.toByte() -> MACHO
                else -> null
            }
        }

        /**
         * Detects the type of the executable file from the provided [ByteArray].
         *
         * @param data the data to read the file header.
         * @return the detected [ExecutableFileType], or `null` if the type could not be detected
         */
        fun detect(data: ByteArray): ExecutableFileType? = detect(ByteArrayDataAccessor(data))
    }

    init {
        require(i in 1..3) { "Unknown executable file type: $i" }
    }

    /**
     * Returns `true` if this is an ELF file.
     *
     * @return `true` if this is an ELF file, `false` otherwise
     */
    fun isElf() = this == ELF

    /**
     * Returns `true` if this is a Mach-O file.
     *
     * @return `true` if this is a Mach-O file, `false` otherwise
     */
    fun isMacho() = this == MACHO

    /**
     * Returns `true` if this is a PE file.
     *
     * @return `true` if this is a PE file, `false` otherwise
     */
    fun isPE() = this == PE

    override fun toString(): String {
        return when (this) {
            ELF -> "ELF"
            MACHO -> "Mach-O"
            PE -> "PE"
            else -> throw IllegalArgumentException("Unknown executable file type: $i")
        }
    }
}
