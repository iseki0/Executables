plugins {
    convention
    id("tgenerator")
}

dependencies {
    commonMainApi(project(":common"))
    commonMainImplementation(project(":share"))
    commonMainImplementation(libs.kotlinx.datetime)
}

kotlin {
    sourceSets {
        val commonMain by getting
        val jsMain by getting
        val nonJvmMain by creating {
            dependsOn(commonMain)
            jsMain.dependsOn(this)
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

jigsaw {
    enable("space.iseki.executables.elf")
}

tasks.named("jvmTest") {
    this as Test
    useJUnitPlatform()
}
