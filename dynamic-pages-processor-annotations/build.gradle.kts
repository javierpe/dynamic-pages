plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("maven-publish")
}

kotlin {
    jvm {
        withJava()
    }
}

publishing {
    publications {
        create<MavenPublication>("kotlin") {
            groupId = project.group.toString()
            artifactId = "dynamic-pages-processor-annotations"
            version = project.version.toString()

            afterEvaluate {
                from(components["kotlin"])
            }
        }
    }
}
