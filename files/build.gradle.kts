import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

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
        val nativeFileSupportedTest by creating {
            dependsOn(commonTest.get())
        }
        val nativeFileSupported2Main by creating {
            dependsOn(commonMain.get())
        }
        val nativeFileSupportedMingw64Main by creating {
            dependsOn(commonMain.get())
        }
        mingwX64Main.get().dependsOn(nativeFileSupportedMingw64Main)
        val nativeFileUnsupported by creating {
            dependsOn(commonMain.get())
        }

        fun NamedDomainObjectProvider<KotlinSourceSet>.nf64() {
            get().dependsOn(nativeFileSupportedMain)
            getByName(get().name.removeSuffix("Main") + "Test").dependsOn(nativeFileSupportedTest)
        }

        fun NamedDomainObjectProvider<KotlinSourceSet>.nf32() {
            get().dependsOn(nativeFileSupported2Main)
        }

        fun NamedDomainObjectProvider<KotlinSourceSet>.uf() {
            get().dependsOn(nativeFileUnsupported)
        }

        jsMain.uf()
        wasmJsMain.uf()
        wasmWasiMain.uf()

        // Tier 1
        macosX64Main.nf64()
        macosArm64Main.nf64()
        iosSimulatorArm64Main.nf64()
        iosX64Main.nf64()
        iosArm64Main.nf64()

        // Tier 2
        linuxX64Main.nf64()
        linuxArm64Main.nf64()
        watchosArm32Main.nf32()
        watchosArm64Main.nf32() // why ???
        watchosX64Main.nf64()
        watchosSimulatorArm64Main.nf64()
        tvosSimulatorArm64Main.nf64()
        tvosX64Main.nf64()
        tvosArm64Main.nf64()

        // Tier 3
        androidNativeArm32Main.nf32()
        androidNativeX86Main.nf32()
        androidNativeArm64Main.nf64()
        androidNativeX64Main.nf64()
        watchosDeviceArm64Main.nf64()


    }
}
