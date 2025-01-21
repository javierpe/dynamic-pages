plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(libs.kotlin.poet)
            implementation(libs.ksp.api)
            implementation(libs.kotlin.poet.ksp)
        }
    }
}

apply(from = file("../gradle/publish.gradle.kts"))