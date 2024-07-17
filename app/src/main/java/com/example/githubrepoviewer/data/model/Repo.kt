package com.example.githubrepoviewer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Repo(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("owner") val owner: Owner,
    @SerialName("description") val description: String? = null,
    @SerialName("language") val language: String? = null,
)

@Serializable
data class Owner(
    @SerialName("login") val login: String
)
