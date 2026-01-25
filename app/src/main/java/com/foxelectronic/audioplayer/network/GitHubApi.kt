package com.foxelectronic.audioplayer.network

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API interface for GitHub Releases API
 * Used to fetch the latest release information from a GitHub repository
 */
interface GitHubApi {
    /**
     * Fetches the latest release from a GitHub repository
     *
     * @param owner The repository owner (username or organization)
     * @param repo The repository name
     * @return GitHubRelease object containing release information
     */
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRelease
}
