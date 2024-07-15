package com.example.githubrepoviewer.data

import com.example.githubrepoviewer.data.model.Repo
import com.example.githubrepoviewer.data.model.RepoDetails
import com.example.githubrepoviewer.data.model.UserInfo
import com.example.githubrepoviewer.data.remote.GitHubApi
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AppRepository @Inject constructor(
    private val gitHubApi: GitHubApi,
) {
    suspend fun getRepositories(): List<Repo> {
        return gitHubApi.getUserRepositories()
    }

    suspend fun getRepository(repoId: String): RepoDetails {
        return gitHubApi.getRepositoryDetails(repoId)
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun getRepositoryReadme(
        ownerName: String, repositoryName: String, branchName: String = "Default"
    ): String {
        val readmeResponse = gitHubApi.getRepositoryReadme(ownerName, repositoryName)
        return when (readmeResponse.encoding) {
            "base64" -> {
                val cleanBase64String = readmeResponse.content.replace("\n", "")
                val decodedBytes = Base64.decode(cleanBase64String)
                String(decodedBytes, Charsets.UTF_8)
            }

            "" -> readmeResponse.content
            else -> "Unknown encoding"
        }.replaceMarkdownImageUrls(readmeResponse.downloadUrl)
    }

    suspend fun signIn(): UserInfo {
        return gitHubApi.getUserInfo()
    }

    private fun String.replaceMarkdownImageUrls(downloadUrl: String): String {
        val baseUrl = downloadUrl.substringBeforeLast("/")
        val imageRegex = Regex("!\\[(.*?)]\\((.*?)\\)")

        return imageRegex.replace(this) { matchResult ->
            val imageName = matchResult.groupValues[1]
            val imageUrl = matchResult.groupValues[2]
            if (imageUrl.startsWith("http")) {
                "![${imageName}](${imageUrl})"
            } else {
                "![${imageName}](${baseUrl}/${imageUrl})"
            }
        }
    }
}
