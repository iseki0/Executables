plugins {
    convention
    id("tgenerator")
    `pub-convention`
}

dependencies {
    commonMainApi(project(":common"))
    commonMainImplementation(project(":share"))
    commonMainImplementation(libs.kotlinx.datetime)
}

jigsaw {
    enable("space.iseki.executables.macho")
}

tasks.named("jvmTest") {
    this as Test
    useJUnitPlatform()
} 