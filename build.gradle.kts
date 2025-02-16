plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jetbrains.dokka")
}

allprojects {
    group = "space.iseki.executables"
    version = "0.0.1-SNAPSHOT"
    repositories {
        mavenCentral()
    }
    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

tasks.dokkaHtmlMultiModule {
    moduleName = "executables"
}
