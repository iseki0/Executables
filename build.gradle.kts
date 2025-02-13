plugins {
    alias(libs.plugins.idea.ext)
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

allprojects {
    group = "space.iseki.executables"
    version = "0.1-SNAPSHOT"
    repositories {
        mavenCentral()
    }
    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}
