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
            api(project(":dynamic-pages-processor-annotations"))
            implementation(project(":ksp-common"))

            implementation(libs.ksp.api)
            implementation(libs.kotlin.poet)
            implementation(libs.kotlin.poet.ksp)
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.kotlinx.serialization)
        }
        jvmMain.dependencies {
            implementation(libs.ksp.api)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }
}

apply(from = file("../gradle/publish.gradle.kts"))
