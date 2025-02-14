plugins {
    convention
}

jigsaw {
    enable("space.iseki.executables.base")
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
