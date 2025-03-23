plugins {
    convention
    id("tgenerator")
    `pub-convention`
}

dependencies {
    commonMainApi(project(":common"))
    commonMainImplementation(project(":share"))
    commonMainImplementation(libs.kotlinx.datetime)
    jvmTestImplementation(platform("org.junit:junit-bom:5.10.0"))
    jvmTestImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.named("jvmTest") {
    this as Test
    useJUnitPlatform()
}

jigsaw {
    enable("space.iseki.executables.pe")
}
