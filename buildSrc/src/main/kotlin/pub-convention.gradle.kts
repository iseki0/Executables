import java.net.URI
import java.util.*

plugins {
    `maven-publish`
    signing
}


publishing {
    repositories {
        maven {
            name = "Central"
            afterEvaluate {
                url = if (version.toString().endsWith("SNAPSHOT")) {
                    // uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
                    uri("https://central.sonatype.com/repository/maven-snapshots/")
                } else {
                    // uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                    // uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                    uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
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
            val pubName = name.replaceFirstChar { it.titlecase(Locale.getDefault()) }
            val emptyJavadocJar by tasks.register<Jar>("emptyJavadocJar$pubName") {
                archiveClassifier = "javadoc"
                archiveBaseName = artifactId
            }
            artifact(emptyJavadocJar)
            pom {
                name = "Executables"
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

signing {
    useGpgCmd()
    sign(publishing.publications)
}

val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}

signingTasks.configureEach {
    dependsOn(tasks.withType<Jar>())
}

