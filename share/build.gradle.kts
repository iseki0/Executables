plugins {
    convention
}

jigsaw {
    enable("space.iseki.executables.share")
}

kotlin {
    sourceSets {
        val commonMain by getting
        val jsMain by getting
        val nonJvmMain by creating {
            dependsOn(commonMain)
            jsMain.dependsOn(this)
        }
    }
}
