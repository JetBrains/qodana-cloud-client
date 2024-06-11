plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.22"
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(11)
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    @Suppress("VulnerableLibrariesLocal", "RedundantSuppression")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testImplementation("org.assertj:assertj-core:3.23.1")
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<AbstractPublishToMaven>().all {
        dependsOn("check")
    }
}

publishing {
    publications {
        create<MavenPublication>("qodana-cloud-kotlin-client") {
            val majorVersion: String by project
            val minorVersion: String by project
            val patch: String by project

            group = "org.jetbrains.qodana"
            artifactId = "qodana-cloud-kotlin-client"
            version = "$majorVersion.$minorVersion.$patch"

            from(components["java"])

            pom {
                url.set("https://github.com/JetBrains/qodana-cloud-client")

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://github.com/JetBrains/qodana-cloud-client/blob/main/LICENSE.txt")
                    }
                }

                developers {
                    developer {
                        id.set("mekhails")
                        name.set("Mikhail Shagvaliev")
                        email.set("Mikhail.Shagvaliev@jetbrains.com")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "intellij-dependencies"
            val spaceUsername: String by project
            val spacePassword: String by project

            url = uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
            credentials {
                username = spaceUsername
                password = spacePassword
            }
        }
    }
}