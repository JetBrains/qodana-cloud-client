plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.22"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    @Suppress("VulnerableLibrariesLocal")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testImplementation("org.assertj:assertj-core:3.23.1")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(11)
}

tasks {
    test {
        useJUnitPlatform()
    }
}