plugins {
    convention
    id("tgenerator")
}

dependencies {
    commonMainApi(project(":common"))
    commonMainImplementation(libs.kotlinx.datetime)
    commonMainImplementation(libs.kotlinx.serialization.core)
    commonTestImplementation(libs.kotlinx.serialization.json)
    jvmTestImplementation(platform("org.junit:junit-bom:5.10.0"))
    jvmTestImplementation("org.junit.jupiter:junit-jupiter")
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

tasks.named("jvmTest") {
    this as Test
    useJUnitPlatform()
}

jigsaw {
    enable("space.iseki.executables.pe")
}
