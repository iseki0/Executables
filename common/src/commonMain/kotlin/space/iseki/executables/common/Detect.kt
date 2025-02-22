package space.iseki.executables.common

import kotlin.jvm.JvmStatic

@Deprecated("Use ExecutableFileType instead", ReplaceWith("ExecutableFileType"), level = DeprecationLevel.WARNING)
enum class ExecutableFile {
    PE, ELF, MACHO,
    ;

    companion object {
        /**
         * Detects the type of the executable file from the provided [DataAccessor].
         *
         * @param dataAccessor the data accessor to read the file header.
         * @throws IOException if an I/O error occurs
         * @return the detected [ExecutableFile], or `null` if the type could not be detected
         */
        @Suppress("DEPRECATION")
        @Throws(IOException::class)
        @Deprecated(
            "Use ExecutableFileType.detect instead",
            ReplaceWith("ExecutableFileType.detect(dataAccessor)"),
            level = DeprecationLevel.WARNING
        )
        @JvmStatic
        fun detect(dataAccessor: DataAccessor): ExecutableFile? {
            val buf = ByteArray(4)
            val read = dataAccessor.readAtMost(0, buf, 0, 4)
            if (read < 4) {
                return null
            }
            return when {
                buf[0] == 0x4d.toByte() && buf[1] == 0x5a.toByte() -> PE
                buf[0] == 0x7f.toByte() && buf[1] == 0x45.toByte() && buf[2] == 0x4c.toByte() && buf[3] == 0x46.toByte() -> ELF
                buf[0] == 0xcf.toByte() && buf[1] == 0xfa.toByte() && buf[2] == 0xed.toByte() && buf[3] == 0xfe.toByte() -> MACHO
                else -> null
            }
        }
    }
}

