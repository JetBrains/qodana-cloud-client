plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.22"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(11)
}