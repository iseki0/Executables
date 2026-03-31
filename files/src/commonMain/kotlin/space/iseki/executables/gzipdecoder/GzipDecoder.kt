package space.iseki.executables.gzipdecoder

internal expect object GzipDecoder {
    fun decode(input: ByteArray): ByteArray
}
