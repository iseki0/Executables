plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.multiplatform)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlinx.binary.compatibility.validator)
    implementation(libs.kotlinx.kover)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
//    implementation(libs.kotlin.gradle.plugin)
}

