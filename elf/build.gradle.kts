plugins {
    convention
    id("tgenerator")
}

dependencies {
    commonMainApi(project(":common"))
    commonMainImplementation(project(":share"))
    commonMainImplementation(libs.kotlinx.datetime)
}

jigsaw {
    enable("space.iseki.executables.elf")
}

tasks.named("jvmTest") {
    this as Test
    useJUnitPlatform()
}
