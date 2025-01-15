apply(plugin = "maven-publish")

val javadocJar = tasks.getByName("javadocJar")

configure<PublishingExtension> {
    publications {
        withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set("Dynamic Pages")
                description.set("Modern SDUI for Compose Multiplatform")
                url.set("")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/javierpe/dynamic-pages")
                    connection.set("https://github.com/javierpe/dynamic-pages.git")
                }
                developers {
                    developer {
                        name.set("Francisco Peña")
                        email.set("francisco.javier.p.ramos@gmail.com")
                    }
                }
            }
        }
    }
}