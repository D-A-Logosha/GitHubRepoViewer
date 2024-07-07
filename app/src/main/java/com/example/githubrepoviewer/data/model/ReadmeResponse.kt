package com.example.githubrepoviewer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReadmeResponse(
    @SerialName("name") val name: String,
    @SerialName("path") val path: String,
    @SerialName("content") val content: String,
    @SerialName("encoding") val encoding: String
)
