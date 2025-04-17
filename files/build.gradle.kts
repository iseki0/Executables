import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    convention
    id("tgenerator")
    `pub-convention`
    id("org.jetbrains.kotlinx.atomicfu")
}

jigsaw {
    enable("space.iseki.executables.files")
}

tasks.named("jvmTest") {
    this as Test
    useJUnitPlatform()
}

dependencies {
    commonMainImplementation("space.iseki.purlkt:purlkt:0.0.7")
    commonTestImplementation(libs.kotlinx.serialization.json)
}

atomicfu {
    transformJvm = false // set to false to turn off JVM transformation
//    jvmVariant = "BOTH" // JVM transformation variant: FU,VH, or BOTH
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
        val nativeFileUnsupportedMain by creating {
            dependsOn(commonMain.get())
        }

        val fileAccessTest by creating {
            dependsOn(commonTest.get())
        }

        fun NamedDomainObjectProvider<KotlinSourceSet>.nfTest() = apply {
            getByName(get().name.removeSuffix("Main") + "Test").dependsOn(fileAccessTest)
        }

        fun NamedDomainObjectProvider<KotlinSourceSet>.nf64() = apply {
            get().dependsOn(nativeFileSupportedMain)
            getByName(get().name.removeSuffix("Main") + "Test").dependsOn(nativeFileSupportedTest)
        }

        fun NamedDomainObjectProvider<KotlinSourceSet>.nf32() = apply {
            get().dependsOn(nativeFileSupported2Main)
        }

        fun NamedDomainObjectProvider<KotlinSourceSet>.uf() = apply {
            get().dependsOn(nativeFileUnsupportedMain)
        }

        jsMain.uf()
        wasmJsMain.uf()
        wasmWasiMain.uf()

        // Tier 1
        macosX64Main.nf64().nfTest()
        macosArm64Main.nf64().nfTest()
        iosSimulatorArm64Main.nf64()
        iosX64Main.nf64()
        iosArm64Main.nf64()

        // Tier 2
        linuxX64Main.nf64().nfTest()
        linuxArm64Main.nf64().nfTest()
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
        mingwX64Main.apply { get().dependsOn(nativeFileSupportedMingw64Main) }.nfTest()

        jvmMain.nfTest()
    }
}

dokka {
    dokkaSourceSets.configureEach {
        sourceRoots.map { root ->
            sourceLink {
                val relPath = root.relativeTo(project.layout.projectDirectory.asFile)
                localDirectory = relPath
                val relPathSlash = relPath.toString().replace('\\', '/')
                remoteUrl("https://github.com/iseki0/Executables/tree/master/files/$relPathSlash")
            }
        }
    }
}
