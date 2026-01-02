package com.foxelectronic.audioplayer.data.model

import android.net.Uri

data class Track(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String?,
    val album: String? = null,
    val albumArtPath: String? = null,
    val isFavorite: Boolean = false
)
