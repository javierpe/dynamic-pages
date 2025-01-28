import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    jvm {
        withJava()
    }

    js(IR) {
        nodejs()
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        nodejs()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    watchosX64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    mingwX64()
    linuxX64()
    linuxArm64()
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates("io.github.javierpe", "processor-annotations", "1.0.3")

    pom {
        name = "Dynamic Pages Annotations"
        description = "Dynamic Pages Annotations"
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
