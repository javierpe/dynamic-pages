
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":dynamic_pages_processor_annotations"))
            implementation(project(":ksp_common"))
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.kotlin.reflect)
            implementation(libs.ksp.api)
            implementation(libs.kotlin.poet)
            implementation(libs.kotlin.poet.ksp)
            implementation(libs.kotlinx.serialization)
        }
        jvmMain.dependencies {
            implementation(libs.ksp.api)
        }
    }
}
