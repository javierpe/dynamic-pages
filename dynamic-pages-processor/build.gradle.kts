plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
    id("maven-publish")
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":dynamic-pages-processor-annotations"))
            implementation(project(":ksp-common"))

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

publishing {
    publications {
        create<MavenPublication>("kotlin") {
            groupId = project.group.toString()
            artifactId = "dynamic-pages-processor"
            version = project.version.toString()

            from(components["kotlin"])
        }
    }
}
