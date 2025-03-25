
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

kotlin {
    sourceSets {
        val nonJvmMain by creating {
            dependsOn(commonMain.get())
        }
        jsMain.get().dependsOn(nonJvmMain)
        nativeMain.get().dependsOn(nonJvmMain)
        wasmJsMain.get().dependsOn(nonJvmMain)
        wasmWasiMain.get().dependsOn(nonJvmMain)

        val nativeFileSupportedMain by creating {
            dependsOn(commonMain.get())
        }
        macosX64Main.get().dependsOn(nativeFileSupportedMain)
        macosArm64Main.get().dependsOn(nativeFileSupportedMain)
        iosSimulatorArm64Main.get().dependsOn(nativeFileSupportedMain)
        iosX64Main.get().dependsOn(nativeFileSupportedMain)
        iosArm64Main.get().dependsOn(nativeFileSupportedMain)
        linuxX64Main.get().dependsOn(nativeFileSupportedMain)
        linuxArm64Main.get().dependsOn(nativeFileSupportedMain)
        androidNativeArm64Main.get().dependsOn(nativeFileSupportedMain)
        androidNativeX64Main.get().dependsOn(nativeFileSupportedMain)

        val nativeFileSupportedTest by creating {
            dependsOn(commonTest.get())
        }
        macosX64Test.get().dependsOn(nativeFileSupportedTest)
        macosArm64Test.get().dependsOn(nativeFileSupportedTest)
        iosSimulatorArm64Test.get().dependsOn(nativeFileSupportedTest)
        iosX64Test.get().dependsOn(nativeFileSupportedTest)
        iosArm64Test.get().dependsOn(nativeFileSupportedTest)
        linuxX64Test.get().dependsOn(nativeFileSupportedTest)
        linuxArm64Test.get().dependsOn(nativeFileSupportedTest)
        androidNativeArm64Test.get().dependsOn(nativeFileSupportedTest)
        androidNativeX64Test.get().dependsOn(nativeFileSupportedTest)

        val nativeFileSupportedMingw64Main by creating {
            dependsOn(commonMain.get())
        }
        mingwX64Main.get().dependsOn(nativeFileSupportedMingw64Main)

        val nativeFileSupported2Main by creating {
            dependsOn(commonMain.get())
        }
        watchosArm32Main.get().dependsOn(nativeFileSupported2Main)
        watchosArm64Main.get().dependsOn(nativeFileSupported2Main)
        watchosX64Main.get().dependsOn(nativeFileSupported2Main)
        watchosSimulatorArm64Main.get().dependsOn(nativeFileSupported2Main)
        tvosSimulatorArm64Main.get().dependsOn(nativeFileSupported2Main)
        tvosX64Main.get().dependsOn(nativeFileSupported2Main)
        tvosArm64Main.get().dependsOn(nativeFileSupported2Main)
        androidNativeArm32Main.get().dependsOn(nativeFileSupported2Main)
        androidNativeX86Main.get().dependsOn(nativeFileSupported2Main)
        watchosDeviceArm64Main.get().dependsOn(nativeFileSupported2Main)

        val nativeFileUnsupported by creating {
            dependsOn(commonMain.get())
        }
        jsMain.get().dependsOn(nativeFileUnsupported)
        wasmJsMain.get().dependsOn(nativeFileUnsupported)
        wasmWasiMain.get().dependsOn(nativeFileUnsupported)

    }
}
