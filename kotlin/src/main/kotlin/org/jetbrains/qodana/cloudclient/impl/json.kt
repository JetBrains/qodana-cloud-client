package org.jetbrains.qodana.cloudclient.impl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val QDCloudJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}