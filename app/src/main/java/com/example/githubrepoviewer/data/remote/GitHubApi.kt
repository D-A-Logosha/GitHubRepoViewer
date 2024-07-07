package com.example.githubrepoviewer.data.remote

import com.example.githubrepoviewer.data.model.ReadmeResponse
import com.example.githubrepoviewer.data.model.Repo
import com.example.githubrepoviewer.data.model.RepoDetails
import com.example.githubrepoviewer.data.model.UserInfo
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApi {

    @GET("user/repos")
    suspend fun getUserRepositories(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
    ): List<Repo>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepositoryDetails(
        @Path("owner") owner: String,
        @Path("repo") repoName: String,
    ): RepoDetails

    @GET("repos/{owner}/{repo}/readme")
    suspend fun getRepositoryReadme(
        @Path("owner") owner: String,
        @Path("repo") repoName: String,
    ): ReadmeResponse

    @GET("user")
    suspend fun getUserInfo(): UserInfo
}
