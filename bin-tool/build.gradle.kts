plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

dependencies {
    commonMainImplementation(project(":files"))
}

kotlin {
    mingwX64 {
        binaries {
            executable {
                entryPoint = "space.iseki.executablestool.main"
            }
        }
    }
    linuxX64 {
        binaries {
            executable {
                entryPoint = "space.iseki.executablestool.main"
            }
        }
    }
    linuxArm64 {
        binaries {
            executable {
                entryPoint = "space.iseki.executablestool.main"
            }
        }
    }
    macosX64 {
        binaries {
            executable {
                entryPoint = "space.iseki.executablestool.main"
            }
        }
    }
    macosArm64 {
        binaries {
            executable {
                entryPoint = "space.iseki.executablestool.main"
            }
        }
    }
}
