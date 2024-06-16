import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

val intellijDependencies = "https://packages.jetbrains.team/maven/p/ij/intellij-dependencies"
val libGroup = "org.jetbrains.qodana"
val libName = "qodana-cloud-kotlin-client"

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.15.0-Beta.2"
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
    apiBuild {
        inputJar.value(jar.flatMap { it.archiveFile })
    }

    // On CI this task is separately executed before publish (to update a version in gradle.properties)
    register("updatePublishVersionInGradleProperties") {
        doLast {
            val versionToPublish = obtainVersionToPublish()
            updatePublishVersionInGradleProperties(versionToPublish)
        }
    }

    register("validateVersionToPublish") {
        doLast {
            val versionInProperties = getPublishVersionInGradleProperties()
            val versionToPublish = obtainVersionToPublish()
            if (versionInProperties != versionToPublish) {
                error("Version in gradle.properties: ${versionInProperties.semverString()}, must be: ${versionToPublish.semverString()}")
            }
        }
    }

    check {
        doLast("Check that version defined in properties file is valid") {
            val version = getPublishVersionInGradleProperties()
            println("If this version will be published, it will be published as v${version.semverString()}")
        }
    }

    withType<AbstractPublishToMaven>().all {
        dependsOn("check")
        dependsOn("validateVersionToPublish")
    }

    register("publishAndLogStatusToTC") {
        dependsOn("publish")
        doLast {
            val version = obtainVersionToPublish()
            println("##teamcity[buildStatus text = 'Published qodana-cloud-kotlin-client:${version.semverString()}']")
        }
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>(libName) {
            group = libGroup
            artifactId = libName
            version = getPublishVersionInGradleProperties().semverString()

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

            url = uri(intellijDependencies)
            credentials {
                username = spaceUsername
                password = spacePassword
            }
        }
    }
}

fun getPublishVersionInGradleProperties(): Version {
    val majorVersion: String by project
    val minorVersion: String by project
    val patchVersion: String by project

    return Version(
        major = majorVersion.toInt(),
        minor = minorVersion.toInt(),
        patch = patchVersion.toInt()
    )
}

fun updatePublishVersionInGradleProperties(newVersion: Version) {
    val gradlePropertiesFile = File("gradle.properties")
    val properties = Properties()
    properties.load(gradlePropertiesFile.inputStream())
    properties.apply {
        setProperty("majorVersion", newVersion.major.toString())
        setProperty("minorVersion", newVersion.minor.toString())
        setProperty("patchVersion", newVersion.patch.toString())
    }
    properties.store(gradlePropertiesFile.outputStream(), null)
}

fun obtainVersionToPublish(): Version {
    val mavenMetadata = currentPublishedMavenMetadata()
    val currentVersion = latestVersionFromMavenMetadata(mavenMetadata)

    val versionInProperties = getPublishVersionInGradleProperties()

    val majorVersionToPublish = versionInProperties.major
    val minorVersionToPublish = versionInProperties.minor
    val patchVersionToPublish: Int

    val isMajorVersionIncremented = majorVersionToPublish - currentVersion.major == 1
    val isMajorVersionSame = majorVersionToPublish == currentVersion.major

    val isMinorVersionIncremented = minorVersionToPublish - currentVersion.minor == 1
    val isMinorVersionSame = minorVersionToPublish == currentVersion.minor

    when {
        isMajorVersionIncremented -> {
            patchVersionToPublish = 0
            if (minorVersionToPublish != 0) {
                error("major version was upgraded to $majorVersionToPublish, set minor version to 0")
            }
        }
        isMajorVersionSame -> {
            when {
                isMinorVersionIncremented -> {
                    patchVersionToPublish = 0
                }
                isMinorVersionSame -> {
                    patchVersionToPublish = currentVersion.patch + 1
                }
                else -> {
                    error("current minor version ${currentVersion.minor}, want to publish $minorVersionToPublish")
                }
            }
        }
        else -> {
            error("current major version ${currentVersion.major}, want to publish $majorVersionToPublish")
        }
    }
    return Version(
        major = majorVersionToPublish,
        minor = minorVersionToPublish,
        patch = patchVersionToPublish
    )
}

fun currentPublishedMavenMetadata(): String {
    val publishedMavenMetadataUrl = URI("$intellijDependencies/")
        .resolve("${libGroup.replace('.', '/')}/")
        .resolve("$libName/")
        .resolve("maven-metadata.xml")

    val request = HttpRequest.newBuilder(publishedMavenMetadataUrl)
        .header("Content-Type", "application/xml")
        .GET()
        .build()

    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    if (response.statusCode() != 200) {
        error("Expected 200 on $publishedMavenMetadataUrl, got status code ${response.statusCode()}, body ${response.body()}")
    }

    return response.body()
}

fun latestVersionFromMavenMetadata(mavenMetadataXml: String): Version {
    val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val document = documentBuilder.parse(mavenMetadataXml.byteInputStream())
    val latestVersion = document.getElementsByTagName("latest").item(0).textContent
    val (major, minor, patch) = latestVersion.split(".").map { it.toInt() }
    return Version(
        major = major,
        minor = minor,
        patch = patch,
    )
}

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
) {
    fun semverString(): String {
        return "$major.$minor.$patch"
    }
}