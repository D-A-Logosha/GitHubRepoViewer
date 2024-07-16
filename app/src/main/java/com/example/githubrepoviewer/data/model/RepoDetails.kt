package com.example.githubrepoviewer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoDetails (
@SerialName("id") val id: Int,
@SerialName("name") val name: String,
@SerialName("owner") val owner: Owner,
@SerialName("html_url") val htmlUrl: String,
@SerialName("license") val license: License?,
@SerialName("stargazers_count") val stargazersCount: Int,
@SerialName("forks_count") val forksCount: Int,
@SerialName("watchers_count") val watchersCount: Int,
)

@Serializable
data class License (
    @SerialName("key") val key: String,
    @SerialName("name") val name: String,
)
