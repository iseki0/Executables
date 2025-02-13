plugins {
    convention
    id("tgenerator")
}

dependencies {
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

tasks.withType<Jar> {
    if ("jvm" in name) {
        manifest {
            attributes["Automatic-Module-Name"] = "space.iseki.executables.base"
        }
    }
}
