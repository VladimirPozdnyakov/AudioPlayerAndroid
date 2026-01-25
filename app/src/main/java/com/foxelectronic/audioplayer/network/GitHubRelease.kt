package com.foxelectronic.audioplayer.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a GitHub release from the API
 * @param tagName Version tag (e.g., "v1.0.0", "0.12b")
 * @param htmlUrl URL to the release page on GitHub
 * @param publishedAt ISO 8601 timestamp when the release was published
 */
@Serializable
data class GitHubRelease(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("published_at")
    val publishedAt: String
)
