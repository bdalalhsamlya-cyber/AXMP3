package com.axmp3

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long = 0L,
    val title: String = "",
    val artist: String = "",
    val duration: Long = 0L,
    val size: Long = 0L,
    val path: String = "",
    val resolution: String = "",
    val uri: Uri = Uri.EMPTY
) : Parcelable
