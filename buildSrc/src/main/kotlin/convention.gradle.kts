import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.kover")
    id("org.jetbrains.dokka")
}

val Project.libs get() = the<org.gradle.accessors.dm.LibrariesForLibs>()

dependencies {
    commonMainImplementation(libs.kotlinx.datetime)
    commonMainImplementation(libs.kotlinx.serialization.core)
    commonMainImplementation(libs.kotlinx.serialization.json)
    commonTestImplementation(libs.kotlinx.serialization.json)
    commonTestImplementation(kotlin("test"))
}

val isGitHubActions = System.getenv("GITHUB_ACTIONS") == "true"
val runnerOs = System.getenv("RUNNER_OS")
val enableAllTargets = !isGitHubActions || runnerOs == "Linux"
val enableAppleTargets = enableAllTargets || runnerOs == "macOS"
val enableWindowsTargets = enableAllTargets || runnerOs == "Windows"
val enableJsTargets = enableAllTargets

kotlin {
    jvmToolchain(24)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
    }
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
            freeCompilerArgs.add("-Xjvm-default=all-compatibility")
        }
    }
    if (enableJsTargets) {
        js {
            browser()
            nodejs()
        }
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmJs {
            browser()
            nodejs()
        }
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmWasi {
            nodejs()
        }
    }

    if (enableAppleTargets) {
        // Tier 1
        macosX64()
        macosArm64()
        iosSimulatorArm64()
        iosX64()
        iosArm64()
    }

    if (enableAllTargets) {
        // Tier 2
        linuxX64()
        linuxArm64()
        watchosArm32()
        watchosArm64()
        watchosX64()
        watchosSimulatorArm64()
        tvosSimulatorArm64()
        tvosX64()
        tvosArm64()
    }

    if (enableAllTargets) {
        // Tier 3
        androidNativeArm32()
        androidNativeArm64()
        androidNativeX64()
        androidNativeX86()
        watchosDeviceArm64()
    }
    if (enableWindowsTargets) {
        mingwX64()
    }

    applyDefaultHierarchyTemplate()
}

tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<Jar> {
    if ("emptyJavadocJar" !in name) {
        into("/") {
            from(rootProject.projectDir.resolve("LICENSE"))
            from(rootProject.projectDir.resolve("NOTICE"))
        }
    }
}


dokka {
    dokkaPublications.configureEach {
        offlineMode = System.getenv("CI") != "true"
    }
}
