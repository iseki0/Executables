plugins {
    convention
    `pub-convention`
}

dependencies {
    commonMainApi(project(":pe"))
    commonMainApi(project(":elf"))
}
