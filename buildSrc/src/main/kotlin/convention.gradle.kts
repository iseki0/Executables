import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI
import java.net.URL
import java.util.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.kover")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
}

val Project.libs get() = the<org.gradle.accessors.dm.LibrariesForLibs>()

dependencies {
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
    targets {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
        jvm {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_1_8
                freeCompilerArgs.add("-Xjvm-default=all-compatibility")
            }
        }
        js {
            browser()
        }
        @OptIn(ExperimentalWasmDsl::class) wasmJs {
            browser()
        }
    }
}

interface Jigsaw {
    fun enable(moduleName: String)
}

open class JigsawImpl(private val project: Project) : Jigsaw {
    override fun enable(moduleName: String) {
        with(project) {
            project.kotlin {
                targets {
                    jvm {
                        withJava()
                    }
                }

            }

            project.java {
                val jpms by sourceSets.creating
                val jvmMain by project.kotlin.sourceSets.getting
                val commonMain by project.kotlin.sourceSets.getting
                with(configurations) {
                    getByName(jpms.implementationConfigurationName).extendsFrom(
                        getByName(jvmMain.implementationConfigurationName),
                        getByName(commonMain.implementationConfigurationName)
                    )
                    getByName(jpms.implementationConfigurationName).extendsFrom(
                        getByName(jvmMain.apiConfigurationName), getByName(commonMain.apiConfigurationName)
                    )
                    getByName(jpms.runtimeOnlyConfigurationName).extendsFrom(
                        getByName(jvmMain.runtimeOnlyConfigurationName),
                        getByName(commonMain.runtimeOnlyConfigurationName)
                    )
                    getByName(jpms.compileOnlyConfigurationName).extendsFrom(
                        getByName(jvmMain.compileOnlyConfigurationName),
                        getByName(commonMain.compileOnlyConfigurationName)
                    )
                }
            }

            tasks.named("jvmJar", Jar::class).configure {
                manifest {
                    attributes["Multi-Release"] = "true"
                }
                into("META-INF/versions/9") {
                    val jpms by sourceSets.getting
                    from(jpms.output)
                }
            }

            tasks.named("compileJpmsJava", JavaCompile::class).configure {
                sourceCompatibility = "9"
                targetCompatibility = "9"
                // todo
//                val clap = CommandLineArgumentProvider {
//                    listOf("--patch-module", "$moduleName=${sourceSets["main"].output.asPath}")
//                }
//                options.compilerArgumentProviders.add(clap)
            }
        }
    }
}

extensions.create(Jigsaw::class.java, "jigsaw", JigsawImpl::class.java, project)

tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
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
        if (!System.getenv("GITHUB_TOKEN").isNullOrBlank()) {
            maven {
                name = "GitHubPackages"
                url = URI.create("https://maven.pkg.github.com/iseki0/executables")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")!!
                    password = System.getenv("GITHUB_TOKEN")!!
                }
            }
        }
    }
    publications {
        withType<MavenPublication> {
            artifactId = "executables-$artifactId"
            val pubName = name.replaceFirstChar { it.titlecase(Locale.getDefault()) }
            val emptyJavadocJar by tasks.register<Jar>("emptyJavadocJar$pubName") {
                archiveClassifier = "javadoc"
                archiveBaseName = artifactId
            }
            artifact(emptyJavadocJar)
            pom {
                val projectUrl = "https://github.com/iseki0/Executables"
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

tasks.withType<Jar> {
    if ("emptyJavadocJar" !in name) {
        into("/") {
            from(rootProject.projectDir.resolve("LICENSE"))
            from(rootProject.projectDir.resolve("NOTICE"))
        }
    }
}

tasks.withType<DokkaTaskPartial> {
    dokkaSourceSets.configureEach {
        includes.from(rootProject.layout.projectDirectory.file("module.md"))
        sourceLink {
            localDirectory = project.layout.projectDirectory.dir("src").asFile
            val p =
                project.layout.projectDirectory.dir("src").asFile.relativeTo(rootProject.layout.projectDirectory.asFile)
                    .toString()
                    .replace('\\', '/')
            remoteUrl = URI.create("https://github.com/iseki0/Executables/tree/master/$p").toURL()
            remoteLineSuffix = "#L"
        }
        externalDocumentationLink {
            url = URL("https://kotlinlang.org/api/kotlinx.serialization/")
        }
    }
}
