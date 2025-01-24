package space.iseki.executables.pe

data class ImageResourceDirectory(
    val characteristics: UInt,
    val timeDataStamp32: TimeDataStamp32,
    val majorVersion: UShort,
    val minorVersion: UShort,
    val numberOfNamedEntries: UShort,
    val numberOfIdEntries: UShort,
) {
    override fun toString(): String {
        return """
            |ImageResourceDirectory(
            |   characteristics = $characteristics,
            |   timeDataStamp32 = $timeDataStamp32,
            |   majorVersion = $majorVersion,
            |   minorVersion = $minorVersion,
            |   numberOfNamedEntries = $numberOfNamedEntries,
            |   numberOfIdEntries = $numberOfIdEntries,
            |)
        """.trimMargin()
    }

    companion object {
        const val LENGTH = 16
        fun parse(data: ByteArray, off: Int): ImageResourceDirectory {
            val characteristics = data.getUInt(off)
            val timeDataStamp32 = TimeDataStamp32(data.getUInt(off + 4))
            val majorVersion = data.getUShort(off + 8)
            val minorVersion = data.getUShort(off + 10)
            val numberOfNamedEntries = data.getUShort(off + 12)
            val numberOfIdEntries = data.getUShort(off + 14)
            return ImageResourceDirectory(
                characteristics,
                timeDataStamp32,
                majorVersion,
                minorVersion,
                numberOfNamedEntries,
                numberOfIdEntries,
            )
        }
    }
}

