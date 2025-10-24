package space.iseki.executables.sbom

import space.iseki.executables.common.FileFormat
import space.iseki.executables.common.open
import kotlin.test.Test
import kotlin.test.assertEquals

class GoVersionTest {
    val base = "src/fileAccessTest/resources/go-version-tc"
    val files = listOf(
        "hello_1.13_darwin_amd64" to "go1.13.15",
        "hello_1.13_linux_amd64" to "go1.13.15",
        "hello_1.13_linux_arm64" to "go1.13.15",
        "hello_1.13_windows_amd64.exe" to "go1.13.15",
        "hello_1.14_darwin_amd64" to "go1.14.15",
        "hello_1.14_linux_amd64" to "go1.14.15",
        "hello_1.14_linux_arm64" to "go1.14.15",
        "hello_1.14_windows_amd64.exe" to "go1.14.15",
        "hello_1.15_darwin_amd64" to "go1.15.15",
        "hello_1.15_linux_amd64" to "go1.15.15",
        "hello_1.15_linux_arm64" to "go1.15.15",
        "hello_1.15_windows_amd64.exe" to "go1.15.15",
        "hello_1.16_darwin_amd64" to "go1.16.15",
        "hello_1.16_darwin_arm64" to "go1.16.15",
        "hello_1.16_linux_amd64" to "go1.16.15",
        "hello_1.16_linux_arm64" to "go1.16.15",
        "hello_1.16_windows_amd64.exe" to "go1.16.15",
        "hello_1.17_darwin_amd64" to "go1.17.13",
        "hello_1.17_darwin_arm64" to "go1.17.13",
        "hello_1.17_linux_amd64" to "go1.17.13",
        "hello_1.17_linux_arm64" to "go1.17.13",
        "hello_1.17_windows_amd64.exe" to "go1.17.13",
        "hello_1.17_windows_arm64.exe" to "go1.17.13",
        "hello_1.18_darwin_amd64" to "go1.18.10",
        "hello_1.18_darwin_arm64" to "go1.18.10",
        "hello_1.18_linux_amd64" to "go1.18.10",
        "hello_1.18_linux_arm64" to "go1.18.10",
        "hello_1.18_windows_amd64.exe" to "go1.18.10",
        "hello_1.18_windows_arm64.exe" to "go1.18.10",
        "hello_1.19_darwin_amd64" to "go1.19.13",
        "hello_1.19_darwin_arm64" to "go1.19.13",
        "hello_1.19_linux_amd64" to "go1.19.13",
        "hello_1.19_linux_arm64" to "go1.19.13",
        "hello_1.19_windows_amd64.exe" to "go1.19.13",
        "hello_1.19_windows_arm64.exe" to "go1.19.13",
        "hello_1.20_darwin_amd64" to "go1.20.14",
        "hello_1.20_darwin_arm64" to "go1.20.14",
        "hello_1.20_linux_amd64" to "go1.20.14",
        "hello_1.20_linux_arm64" to "go1.20.14",
        "hello_1.20_windows_amd64.exe" to "go1.20.14",
        "hello_1.20_windows_arm64.exe" to "go1.20.14",
        "hello_1.21_darwin_amd64" to "go1.21.13",
        "hello_1.21_darwin_arm64" to "go1.21.13",
        "hello_1.21_linux_amd64" to "go1.21.13",
        "hello_1.21_linux_arm64" to "go1.21.13",
        "hello_1.21_windows_amd64.exe" to "go1.21.13",
        "hello_1.21_windows_arm64.exe" to "go1.21.13",
        "hello_1.22_darwin_amd64" to "go1.22.12",
        "hello_1.22_darwin_arm64" to "go1.22.12",
        "hello_1.22_linux_amd64" to "go1.22.12",
        "hello_1.22_linux_arm64" to "go1.22.12",
        "hello_1.22_windows_amd64.exe" to "go1.22.12",
        "hello_1.22_windows_arm64.exe" to "go1.22.12",
        "hello_1.23_darwin_amd64" to "go1.23.12",
        "hello_1.23_darwin_arm64" to "go1.23.12",
        "hello_1.23_linux_amd64" to "go1.23.12",
        "hello_1.23_linux_arm64" to "go1.23.12",
        "hello_1.23_windows_amd64.exe" to "go1.23.12",
        "hello_1.23_windows_arm64.exe" to "go1.23.12",
        "hello_1.24_darwin_amd64" to "go1.24.9",
        "hello_1.24_darwin_arm64" to "go1.24.9",
        "hello_1.24_linux_amd64" to "go1.24.9",
        "hello_1.24_linux_arm64" to "go1.24.9",
        "hello_1.24_windows_amd64.exe" to "go1.24.9",
        "hello_1.24_windows_arm64.exe" to "go1.24.9",
    )

    @Test
    fun testAllVersions() {
        for ((file, version) in files) {
            val path = "$base/$file"
            val sbom = FileFormat.open(path)!!.use { GoSBom.readFrom(it) }
            assertEquals(version, sbom.version)
        }
    }
}
