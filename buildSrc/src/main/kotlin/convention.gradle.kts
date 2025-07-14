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

tasks.withType<JavaCompile> {
    if ("java9" !in name.lowercase()) {
        targetCompatibility = "1.8"
        sourceCompatibility = "1.8"
    }
}

kotlin {
    jvmToolchain(21)
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
    js {
        browser()
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }
    wasmWasi {
        nodejs()
    }

    // Tier 1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()
    iosArm64()

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

    // Tier 3
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()
    mingwX64()
    watchosDeviceArm64()

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

// I don't know why this is needed, but it is.
afterEvaluate {
    if (tasks.findByName("tGenerateFlagFiles") != null) {
        tasks.findByName("dokkaGeneratePublicationHtml")?.dependsOn("tGenerateFlagFiles")
        tasks.findByName("dokkaGenerateModuleHtml")?.dependsOn("tGenerateFlagFiles")
    }
}
