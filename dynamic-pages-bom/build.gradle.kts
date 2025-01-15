import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":dynamic-pages-processor-annotations"))
        api(project(":dynamic-pages-mapper-processor"))
        api(project(":dynamic-pages-processor"))
    }
}

apply(from = file("../gradle/publish-pom.gradle.kts"))