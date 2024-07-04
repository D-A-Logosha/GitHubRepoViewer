package com.example.githubrepoviewer.data

import com.example.githubrepoviewer.data.model.Repo
import com.example.githubrepoviewer.data.model.RepoDetails
import com.example.githubrepoviewer.data.model.UserInfo

class AppRepository {
    suspend fun getRepositories(): List<Repo> {
        TODO()
    }

    suspend fun getRepository(repoId: String): RepoDetails {
        TODO()
    }

    suspend fun getRepositoryReadme(ownerName: String, repositoryName: String, branchName: String): String {
        TODO()
    }

    suspend fun signIn(token: String): UserInfo {
        TODO()
    }

    // TODO:
}
