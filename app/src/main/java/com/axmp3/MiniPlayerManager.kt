package com.axmp3

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.util.ArrayList

class MiniPlayerManager(
private val context: Context,
private val viewBuilder: MainViewBuilder,
private val onMiniPlayerClick: (Int) -> Unit
) {

private val miniPlayerView = LinearLayout(context)
private val songTitleTxt = TextView(context)
private val playPauseBtn = ImageView(context)
private val nextBtn = ImageView(context)

private var songsList = ArrayList<Song>()
private var currentPosition = 0
private var isPlaying = false

init {
buildMiniPlayerUI()
}

private fun buildMiniPlayerUI() {
miniPlayerView.apply {
layoutParams = FrameLayout.LayoutParams(
FrameLayout.LayoutParams.MATCH_PARENT,
dpToPx(65)
).apply {
gravity = Gravity.BOTTOM
}
orientation = LinearLayout.HORIZONTAL
gravity = Gravity.CENTER_VERTICAL
setBackgroundColor(Color.parseColor("#795548"))
setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
}

val albumArtImg = ImageView(context).apply {
layoutParams = LinearLayout.LayoutParams(dpToPx(45), dpToPx(45))
setImageResource(android.R.drawable.ic_media_play)
scaleType = ImageView.ScaleType.CENTER_CROP
}

songTitleTxt.apply {
layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
setMargins(dpToPx(12), 0, dpToPx(12), 0)
}
text = "لا توجد أغنية قيد التشغيل"
setTextColor(Color.WHITE)
textSize = 14f
maxLines = 1
}

playPauseBtn.apply {
layoutParams = LinearLayout.LayoutParams(dpToPx(35), dpToPx(35)).apply {
setMargins(0, 0, dpToPx(12), 0)
}
setImageResource(android.R.drawable.ic_media_play)
setOnClickListener {
isPlaying = !isPlaying
updatePlayPauseIcon()
}
}

nextBtn.apply {
layoutParams = LinearLayout.LayoutParams(dpToPx(35), dpToPx(35))
setImageResource(android.R.drawable.ic_media_next)
setOnClickListener {
if (songsList.isNotEmpty() && currentPosition < songsList.size - 1) {
currentPosition++
updateUI(songsList[currentPosition])
}
}
}

miniPlayerView.addView(albumArtImg)
miniPlayerView.addView(songTitleTxt)
miniPlayerView.addView(playPauseBtn)
miniPlayerView.addView(nextBtn)

miniPlayerView.setOnClickListener {
onMiniPlayerClick(currentPosition)
}

viewBuilder.miniPlayerContainer.removeAllViews()
viewBuilder.miniPlayerContainer.addView(miniPlayerView)
}

fun setSongList(list: List<Song>) {
songsList.clear()
songsList.addAll(list)
}

fun playSongAt(position: Int) {
if (songsList.isNotEmpty() && position in songsList.indices) {
currentPosition = position
isPlaying = true
viewBuilder.miniPlayerContainer.visibility = View.VISIBLE
updateUI(songsList[position])
}
}

private fun updateUI(song: Song) {
songTitleTxt.text = song.title
updatePlayPauseIcon()
}

private fun updatePlayPauseIcon() {
val icon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
playPauseBtn.setImageResource(icon)
}

fun unbind() {}

private fun dpToPx(dp: Int): Int {
val density = context.resources.displayMetrics.density
return (dp * density).toInt()
}
}