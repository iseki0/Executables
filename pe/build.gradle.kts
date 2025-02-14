plugins {
    convention
    id("tgenerator")
}

dependencies {
    commonMainApi(project(":base"))
    commonMainImplementation(libs.kotlinx.datetime)
    commonMainImplementation(libs.kotlinx.serialization.core)
    commonTestImplementation(libs.kotlinx.serialization.json)
}

kotlin {
    sourceSets {
        val commonMain by getting
        val jsMain by getting
        val wasmJsMain by getting
        val nonJvmMain by creating {
            dependsOn(commonMain)
            jsMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

jigsaw {
    enable("space.iseki.executables.pe")
}
