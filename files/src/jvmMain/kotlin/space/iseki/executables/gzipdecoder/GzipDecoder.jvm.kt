package space.iseki.executables.gzipdecoder

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream

internal actual object GzipDecoder {
    actual fun decode(input: ByteArray): ByteArray {
        ByteArrayInputStream(input).use { inputStream ->
            GZIPInputStream(inputStream).use { gzipInputStream ->
                val outputStream = ByteArrayOutputStream(input.size.coerceAtLeast(32))
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = gzipInputStream.read(buffer)
                    if (read < 0) break
                    if (read == 0) continue
                    outputStream.write(buffer, 0, read)
                }
                return outputStream.toByteArray()
            }
        }
    }
}
