package com.example.githubrepoviewer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    @SerialName("id") val id: Int,
    @SerialName("node_id") val nodeId: String,
    @SerialName("login") val login: String,
)
