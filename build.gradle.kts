import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.*

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
    alias(libs.plugins.kotlinx.kover)
    `maven-publish`
    signing
    id("tgenerator")
}

allprojects {
    group = "space.iseki.executables"
    version = "0.1-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}

dependencies {
    commonMainImplementation(libs.kotlinx.datetime)
    commonMainImplementation(libs.kotlinx.serialization.core)
    commonTestImplementation(libs.kotlinx.serialization.json)
    commonTestImplementation(kotlin("test"))
}

kotlin {
    targets {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
        jvm {
            compilerOptions {
                freeCompilerArgs.add("-Xjvm-default=all")
            }
        }
        js {
            browser()
        }
        @OptIn(ExperimentalWasmDsl::class) wasmJs {
            browser()
        }
    }

    sourceSets {
        val commonMain by getting
        val jsMain by getting
        val wasmJsMain by getting
        val nonJvmMain by creating {
            dependsOn(commonMain)
            jsMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<Jar> {
    if ("jvm" in name) {
        manifest {
            attributes["Automatic-Module-Name"] = "space.iseki.executables"
        }
    }
    if ("emptyJavadocJar" !in name) {
        into("/") {
            from("/LICENSE")
            from("/NOTICE")
        }
    }
}

tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
    }
}

publishing {
    repositories {
        maven {
            name = "Central"
            afterEvaluate {
                url = if (version.toString().endsWith("SNAPSHOT")) {
                    // uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
                    uri("https://oss.sonatype.org/content/repositories/snapshots")
                } else {
                    // uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                    uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                }
            }
            credentials {
                username = properties["ossrhUsername"]?.toString() ?: System.getenv("OSSRH_USERNAME")
                password = properties["ossrhPassword"]?.toString() ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
    publications {
        withType<MavenPublication> {
            val pubName = name.replaceFirstChar { it.titlecase(Locale.getDefault()) }
            val emptyJavadocJar by tasks.register<Jar>("emptyJavadocJar$pubName") {
                archiveClassifier = "javadoc"
                archiveBaseName = artifactId
            }
            artifact(emptyJavadocJar)
            pom {
                val projectUrl = "https://github.com/iseki0/Executables"
                name = "Executables"
                description = "A library for executable files"
                url = projectUrl
                licenses {
                    license {
                        name = "Apache-2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
                developers {
                    developer {
                        id = "iseki0"
                        name = "iseki zero"
                        email = "iseki@iseki.space"
                    }
                }
                inceptionYear = "2024"
                scm {
                    connection = "scm:git:$projectUrl.git"
                    developerConnection = "scm:git:$projectUrl.git"
                    url = projectUrl
                }
                issueManagement {
                    system = "GitHub"
                    url = "$projectUrl/issues"
                }
                ciManagement {
                    system = "GitHub"
                    url = "$projectUrl/actions"
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}
