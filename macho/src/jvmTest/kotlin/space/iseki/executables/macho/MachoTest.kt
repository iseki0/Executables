package space.iseki.executables.macho

import java.nio.file.Path
import kotlin.test.Test

class MachoTest {
    @Test
    fun test() {
        MachoFile.open(Path.of("src/jvmTest/resources/tiny-macho")).use {
            println(it.header)
        }
    }
}
