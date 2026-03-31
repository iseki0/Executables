package space.iseki.executables.gzipdecoder

internal actual object GzipDecoder {
    actual fun decode(input: ByteArray): ByteArray = InternalGzipDecoder.decode(input)
}
