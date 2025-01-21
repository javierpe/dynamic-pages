import com.vanniktech.maven.publish.SonatypeHost

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
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlinx.serialization)
        }
        jvmMain.dependencies {
            implementation(project(":dynamic-pages-processor-annotations"))
            implementation(project(":ksp-common"))

            implementation(libs.ksp.api)
            implementation(libs.kotlin.poet)
            implementation(libs.kotlin.poet.ksp)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates("dynamic-pages", "processor", "1.0.0")

    pom {
        name = "Dynamic Pages Processor"
        description = "Dynamic Pages Processor"
        inceptionYear = "2025"
        url = "https://github.com/javierpe/dynamic-pages"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "javierpe"
                name = "Francisco Peña"
                url = "https://github.com/javierpe/"
            }
        }
        scm {
            url = "https://github.com/javierpe/dynamic-pages"
            connection = "scm:git:git://github.com/javierpe/dynamic-pages"
            developerConnection = "scm:git:ssh://git@github.com/javierpe/dynamic-pages.git"
        }
    }
}
