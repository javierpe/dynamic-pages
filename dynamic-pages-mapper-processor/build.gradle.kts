plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":ksp-common"))
            implementation(libs.ksp.api)
            implementation(libs.kotlin.poet)
            implementation(libs.kotlin.poet.ksp)
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(project(":dynamic-pages-processor-annotations"))
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }
}

apply(from = file("../gradle/publish.gradle.kts"))
