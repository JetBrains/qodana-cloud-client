package org.jetbrains.qodana.cloudclient

sealed interface QDCloudRequestMethod {
    val parameters: Map<String, String>

    data class GET(
        override val parameters: Map<String, String> = emptyMap()
    ) : QDCloudRequestMethod

    data class POST(
        val body: String = "",
        override val parameters: Map<String, String> = emptyMap()
    ) : QDCloudRequestMethod

    data class DELETE(
        override val parameters: Map<String, String> = emptyMap()
    ) : QDCloudRequestMethod

    data class PUT(
        val body: String = "",
        override val parameters: Map<String, String> = emptyMap()
    ) : QDCloudRequestMethod

    data class Other(
        val name: String,
        val body: String = "",
        override val parameters: Map<String, String> = emptyMap()
    ) : QDCloudRequestMethod
}