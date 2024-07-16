package com.example.githubrepoviewer.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

internal fun HttpException.getErrorMessage(): String? {
    val errorJsonString = response()?.errorBody()?.string()
    return runCatching {
        if (!errorJsonString.isNullOrEmpty()) {
            val json = Json { ignoreUnknownKeys = true }
            val errorJsonObject = json.parseToJsonElement(errorJsonString).jsonObject
            errorJsonObject["message"]?.jsonPrimitive?.content
        } else null
    }.getOrNull()
}