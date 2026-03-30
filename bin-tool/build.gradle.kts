plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

dependencies {
    commonMainImplementation(project(":executables-files"))
}

val isGitHubActions = System.getenv("GITHUB_ACTIONS") == "true"
val runnerOs = System.getenv("RUNNER_OS")
val enableAllTargets = !isGitHubActions || runnerOs == "Linux"
val enableAppleTargets = enableAllTargets || runnerOs == "macOS"
val enableWindowsTargets = enableAllTargets || runnerOs == "Windows"

kotlin {
    if (enableWindowsTargets) {
        mingwX64 {
            binaries {
                executable {
                    entryPoint = "space.iseki.executablestool.main"
                }
            }
        }
    }
    if (enableAllTargets) {
        linuxX64 {
            binaries {
                executable {
                    entryPoint = "space.iseki.executablestool.main"
                }
            }
        }
    }
    if (enableAllTargets) {
        linuxArm64 {
            binaries {
                executable {
                    entryPoint = "space.iseki.executablestool.main"
                }
            }
        }
    }
    if (enableAppleTargets) {
        macosArm64 {
            binaries {
                executable {
                    entryPoint = "space.iseki.executablestool.main"
                }
            }
        }
    }
}
