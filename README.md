# Qodana Cloud API client libraries

[![official JetBrains project](https://jb.gg/badges/official.svg)][jb:confluence-on-gh]
[![Qodana](https://github.com/JetBrains/qodana-cloud-client/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/JetBrains/qodana-cloud-client/actions/workflows/qodana_code_quality.yml)
[![GitHub Discussions](https://img.shields.io/github/discussions/jetbrains/qodana)][jb:discussions]
[![Twitter Follow](https://img.shields.io/badge/follow-%40Qodana-1DA1F2?logo=twitter&style=social)][jb:twitter]

Client libraries for [Qodana Cloud](https://qodana.cloud/) API with support for different Qodana Cloud versions

Libraries available for:
- [Kotlin](#Kotlin)


### Why 
With self-hosted, we face a challenge: backwards compatibility of Qodana Cloud clients with old Qodana Cloud versions.
Example: the latest Qodana IDE plugin wants to use API available only in the latest Qodana Cloud, but the user has old self-hosted Qodana Cloud

Solution: API client libraries built with support of Qodana Cloud versions declared in library's API: 
while working with a library's API, you are aware of which Qodana Cloud version is currently available and 
able to request a different version (for example, require v1.5 which has an API needed for the feature) 



## Kotlin

### Adding to your project

Add the following to the `build.gradle.kts`:

```kotlin
val qodanaCloudClientVersion = "0.1.2"

repositories {
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
}

dependencies {
    implementation("org.jetbrains.qodana:qodana-cloud-kotlin-client:$qodanaSarifVersion")
}
```

### Working with Qodana Cloud API

```kotlin
suspend fun main() {
    val client = createClient()

    processUserName(client)
    processProjectName(client)
}

private fun createClient(): QDCloudClient {
    val httpClient = QDCloudHttpClient(HttpClient.newHttpClient())
    val environment = QDCloudEnvironment("https://qodana.cloud", httpClient).requestOn(GlobalScope)
    return QDCloudClient(httpClient, environment)
}

private suspend fun processUserName(client: QDCloudClient) {
    val userName = qodanaCloudResponse {
        val userApi = client.v1().value().userApi(::userToken)
        val v5 = userApi.v5
        val v3 = userApi.v3
        when {
            v5 != null -> {
                // v5-specific call v5.xxx().value()
                // here you can access APIs: (v1.3, v1) like this: v5.v3.xxx(), v5.base.xxx()
                "user name from V5"
            }
            v3 != null -> {
                // v3-specific call v3.xxx().value()
                // here you can access APIs: (v1) like this: v3.base.xxx()
                "user name from V3"
            }
            else -> {
                userApi.getUserInfo().value().fullName
            }
        }
    }
    when(userName) {
        is QDCloudResponse.Success -> {
            val name = userName.value
            println("Username is $name")
        }
        is QDCloudResponse.Error.Offline -> {
            println("Failed to obtain username, qodana cloud is not available")
        }
        is QDCloudResponse.Error.ResponseFailure -> {
            println("Failed to obtain username, code: ${userName.responseCode}, message: ${userName.errorMessage}")
        }
    }
}

private suspend fun processProjectName(client: QDCloudClient) {
    val projectName = qodanaCloudResponse {
        val projectApi = client.v1().value().projectApi("token")
        val v5 = projectApi.v5
        val v3 = projectApi.v3
        when {
            v5 != null -> {
                // v5-specific call v5.xxx().value()
                // here you can access APIs: (v1.3, v1) like this: v5.v3.xxx(), v5.base.xxx()
                "project name from V5"
            }
            v3 != null -> {
                // v3-specific call v3.xxx().value()
                // here you can access APIs: (v1) like this: v3.base.xxx()
                "project name from V3"
            }
            else -> {
                projectApi.getProjectProperties().value().name
            }
        }
    }
    when(projectName) {
        is QDCloudResponse.Success -> {
            val name = projectName.value
            println("Project name is $name")
        }
        is QDCloudResponse.Error.Offline -> {
            println("Failed to obtain project name, qodana cloud is not available")
        }
        is QDCloudResponse.Error.ResponseFailure -> {
            println("Failed to obtain project name, code: ${projectName.responseCode}, message: ${projectName.errorMessage}")
        }
    }
}

private fun userToken(): QDCloudResponse.Success<String> {
    return QDCloudResponse.Success("user-token")
}
```

[gh:qodana]: https://github.com/JetBrains/qodana-action/actions/workflows/code_scanning.yml

[youtrack]: https://youtrack.jetbrains.com/issues/QD

[youtrack-new-issue]: https://youtrack.jetbrains.com/newIssue?project=QD&c=Platform%20GitHub%20action

[jb:confluence-on-gh]: https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub

[jb:discussions]: https://jb.gg/qodana-discussions

[jb:twitter]: https://twitter.com/Qodana

[jb:docker]: https://hub.docker.com/r/jetbrains/qodana