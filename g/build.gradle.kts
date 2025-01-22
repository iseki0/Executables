plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.idea.ext)
    implementation(libs.freemarker)
    implementation(libs.kaml)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlinx.coroutines.core)
}

gradlePlugin{
    plugins {
        create("t-generator") {
            id = "tgenerator"
            implementationClass = "a.G"
        }
    }
}
