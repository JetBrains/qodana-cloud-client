package org.jetbrains.qodana.cloudclient

data class QDCloudRequest(
    val path: String,
    val type: Type,
    val parameters: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap()
) {
    sealed interface Type

    data object GET : Type

    data class POST(val body: String? = null) : Type

    data object DELETE : Type

    data class PUT(val body: String? = null) : Type

    data class Other(val name: String, val body: String? = null) : Type
}