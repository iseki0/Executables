plugins {
    convention
}

dependencies {
    commonMainApi(project(":pe"))
    commonMainApi(project(":elf"))
    commonMainApi(project(":macho"))
}
