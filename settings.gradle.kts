plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "executables"
includeBuild("g")
include("files")
include("bin-tool")

rootProject.children.find { it.name == "files" }!!.name = "executables-files"
