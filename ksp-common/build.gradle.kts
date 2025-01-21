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
        commonMain.dependencies {
            implementation(libs.kotlin.poet)
            implementation(libs.ksp.api)
            implementation(libs.kotlin.poet.ksp)
        }
        jvmMain.dependencies {
            implementation(libs.ksp.api)
        }
    }
}
