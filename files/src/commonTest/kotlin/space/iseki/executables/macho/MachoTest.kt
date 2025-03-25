package space.iseki.executables.macho

import kotlin.test.Test

class MachoTest {
    @Test
    fun test() {
        try {
            MachoFile.open("src/commonTest/resources/tiny-macho").use {
                println(it.header)
            }
        } catch (e: UnsupportedOperationException) {
        }
    }
}
