package org.jetbrains.qodana.cloudclient

class QDCloudRequest(
    val path: String,
    val type: Type,
    val parameters: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap()
) {
    sealed interface Type

    data object GET : Type

    class POST(val body: String? = null) : Type

    data object DELETE : Type

    class PUT(val body: String? = null) : Type

    class Other(val name: String, val body: String? = null) : Type
}