package space.iseki.executables.pe

/**
 * Represents an image resource directory in a pe file.
 *
 * @property characteristics the characteristics of the directory
 * @property timeDateStamp32 the time date stamp of the directory
 * @property majorVersion the major version number
 * @property minorVersion the minor version number
 * @property numberOfNamedEntries the number of named entries
 * @property numberOfIdEntries the number of id entries
 */
data class ImageResourceDirectory(
    val characteristics: UInt,
    val timeDateStamp32: TimeDateStamp32,
    val majorVersion: UShort,
    val minorVersion: UShort,
    val numberOfNamedEntries: UShort,
    val numberOfIdEntries: UShort,
) {
    override fun toString(): String =
        fields.entries.joinToString("", "ImageResourceDirectory(", ")") { (k, v) -> "   $k = $v,\n" }

    val fields: Map<String, Any>
        get() = mapOf(
            "characteristics" to characteristics,
            "timeDateStamp32" to timeDateStamp32,
            "majorVersion" to majorVersion,
            "minorVersion" to minorVersion,
            "numberOfNamedEntries" to numberOfNamedEntries,
            "numberOfIdEntries" to numberOfIdEntries,
        )

    companion object {
        const val LENGTH = 16

        /**
         * Parses an image resource directory from the given byte array starting at the specified offset.
         *
         * @param data the byte array containing the directory data
         * @param off the offset at which the directory starts
         * @return an image resource directory instance
         */
        fun parse(data: ByteArray, off: Int): ImageResourceDirectory {
            val characteristics = data.getUInt(off)
            val timeDateStamp32 = TimeDateStamp32(data.getUInt(off + 4))
            val majorVersion = data.getUShort(off + 8)
            val minorVersion = data.getUShort(off + 10)
            val numberOfNamedEntries = data.getUShort(off + 12)
            val numberOfIdEntries = data.getUShort(off + 14)
            return ImageResourceDirectory(
                characteristics,
                timeDateStamp32,
                majorVersion,
                minorVersion,
                numberOfNamedEntries,
                numberOfIdEntries,
            )
        }
    }
}

