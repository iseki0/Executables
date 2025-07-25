plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.multiplatform)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlinx.kover)
    implementation(libs.kotlinx.atomicfu)
    implementation(libs.dokka)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
//    implementation(libs.kotlin.gradle.plugin)
}

