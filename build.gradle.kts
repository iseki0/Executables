plugins {
    kotlin("multiplatform") version "2.1.0"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.17.0"
}

allprojects {
    group = "space.iseki.executables"
    version = "1.0-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}

dependencies {
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    commonTestImplementation(kotlin("test"))
}

kotlin {
    targets {
        jvm()
        js {
            browser()
        }
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Automatic-Module-Name"] = "space.iseki.executables"
    }
    into("/") {
        from("/LICENSE")
        from("/NOTICE")
    }
}

tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
    }
}
