package com.axmp3

import android.content.ContentUris
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class SearchActivity : AppCompatActivity(), SongAdapter.OnSongClickListener {

private lateinit var searchResults: RecyclerView
private lateinit var searchInput: EditText
private lateinit var adapter: SongAdapter

private val allSongs = mutableListOf<Song>()
private val filteredSongs = mutableListOf<Song>()

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)

val mainLayout = LinearLayout(this).apply {
layoutParams = ViewGroup.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.MATCH_PARENT
)
orientation = LinearLayout.VERTICAL
setBackgroundColor(Color.parseColor("#FF0A0A0A"))
}

val toolbar = LinearLayout(this).apply {
layoutParams = LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
dpToPx(56)
)
orientation = LinearLayout.HORIZONTAL
gravity = Gravity.CENTER_VERTICAL
setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
setBackgroundColor(Color.parseColor("#FF1A1A2E"))
}

val searchBack = ImageView(this).apply {
layoutParams = LinearLayout.LayoutParams(dpToPx(40), dpToPx(40))
setImageResource(android.R.drawable.ic_menu_revert)
setColorFilter(Color.parseColor("#FFD700"), PorterDuff.Mode.SRC_IN)
setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
setOnClickListener { finish() }
}
toolbar.addView(searchBack)

searchInput = EditText(this).apply {
layoutParams = LinearLayout.LayoutParams(0, dpToPx(40), 1f)
hint = "ابحث عن أغنية..."
setTextColor(Color.WHITE)
setHintTextColor(Color.parseColor("#888888"))
setBackgroundColor(Color.TRANSPARENT)
setPadding(dpToPx(8), 0, dpToPx(8), 0)
maxLines = 1
isSingleLine = true
}
toolbar.addView(searchInput)
mainLayout.addView(toolbar)

searchResults = RecyclerView(this).apply {
id = View.generateViewId()
layoutParams = LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.MATCH_PARENT
)
setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
clipToPadding = false
layoutManager = LinearLayoutManager(this@SearchActivity)
}
mainLayout.addView(searchResults)

setContentView(mainLayout)

adapter = SongAdapter(this, filteredSongs, this)
searchResults.adapter = adapter

loadAllSongs()

searchInput.addTextChangedListener(object : TextWatcher {
override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
filterSongs(s.toString())
}
override fun afterTextChanged(s: Editable?) {}
})
}

private fun loadAllSongs() {
val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
val projection = arrayOf(
MediaStore.Audio.Media._ID,
MediaStore.Audio.Media.TITLE,
MediaStore.Audio.Media.ARTIST,
MediaStore.Audio.Media.DURATION,
MediaStore.Audio.Media.SIZE,
MediaStore.Audio.Media.DATA
)

contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

while (cursor.moveToNext()) {
val id = cursor.getLong(idColumn)
val title = cursor.getString(titleColumn) ?: "Unknown"
val artist = cursor.getString(artistColumn) ?: "<unknown>"
val duration = cursor.getLong(durationColumn)
val size = cursor.getLong(sizeColumn)
val path = cursor.getString(dataColumn) ?: ""
val songUri = ContentUris.withAppendedId(uri, id)

// 🛠️ تم تصحيح بناء الكائن وحذف folderName و resolution لتتوافق تماماً مع الموديل الموحد
allSongs.add(
Song(
id = id,
title = title,
artist = artist,
duration = duration,
size = size,
path = path,
uri = songUri
)
)
}
}
filteredSongs.clear()
filteredSongs.addAll(allSongs)
adapter.notifyDataSetChanged()
}

private fun filterSongs(query: String) {
filteredSongs.clear()
if (query.isEmpty()) {
filteredSongs.addAll(allSongs)
} else {
val lowerCaseQuery = query.lowercase()
for (s in allSongs) {
if (s.title.lowercase().contains(lowerCaseQuery) || s.artist.lowercase().contains(lowerCaseQuery)) {
filteredSongs.add(s)
}
}
}
adapter.notifyDataSetChanged()
}

override fun onSongClick(song: Song, position: Int) {
val intent = Intent(this, NowPlayingActivity::class.java).apply {
putExtra("song_index", position)
putExtra("song_list", ArrayList(filteredSongs))
}
startActivity(intent)
}

private fun dpToPx(dp: Int): Int {
return (dp * resources.displayMetrics.density).toInt()
}
}