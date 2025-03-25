plugins {
    convention
    id("tgenerator")
    `pub-convention`
}

jigsaw {
    enable("space.iseki.executables.files")
}

tasks.named("jvmTest") {
    this as Test
    useJUnitPlatform()
}
