package com.axmp3

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.util.ArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaLoader(private val context: Context) {

fun getAlbumArt(path: String): String = path
fun getAlbumArt(song: Song): String = song.path

// تحويل الدالة إلى suspend لضمان انتهاء جلب البيانات قبل العودة للـ MainActivity
suspend fun loadSongs(): ArrayList<Song> = withContext(Dispatchers.IO) {
val tempSongList = ArrayList<Song>()
val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
val projection = arrayOf(
MediaStore.Audio.Media._ID,
MediaStore.Audio.Media.TITLE,
MediaStore.Audio.Media.ARTIST,
MediaStore.Audio.Media.DURATION,
MediaStore.Audio.Media.SIZE,
MediaStore.Audio.Media.DATA
)

// تصفية لجلب ملفات الموسيقى الحقيقية فقط والتي حجمها أكبر من صفر
val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

try {
val cursor: Cursor? = context.contentResolver.query(
uri,
projection,
selection,
null,
"${MediaStore.Audio.Media.TITLE} ASC"
)
cursor?.use {
val idCol = it.getColumnIndex(MediaStore.Audio.Media._ID)
val titleCol = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
val artistCol = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
val durationCol = it.getColumnIndex(MediaStore.Audio.Media.DURATION)
val sizeCol = it.getColumnIndex(MediaStore.Audio.Media.SIZE)
val dataCol = it.getColumnIndex(MediaStore.Audio.Media.DATA)

while (it.moveToNext()) {
val id = if (idCol != -1) it.getLong(idCol) else 0L
val title = if (titleCol != -1) it.getString(titleCol) ?: "Unknown" else "Unknown"
val artist = if (artistCol != -1) it.getString(artistCol) ?: "<unknown>" else "<unknown>"
val duration = if (durationCol != -1) it.getLong(durationCol) else 0L
val size = if (sizeCol != -1) it.getLong(sizeCol) else 0L
val path = if (dataCol != -1) it.getString(dataCol) ?: "" else ""

// توليد الـ Uri بشكل نقي وصحيح ليعمل مع السيرفس الجديد
val contentUri = ContentUris.withAppendedId(uri, id)

tempSongList.add(
Song(
id = id,
title = title,
artist = artist,
duration = duration,
size = size,
path = path,
resolution = "",
uri = contentUri
)
)
}
}
} catch (e: Exception) {
e.printStackTrace()
}
return@withContext tempSongList
}

// تحويل دالة الفيديوهات إلى suspend لمنع تسريب القوائم الفارغة
suspend fun loadVideos(): ArrayList<Video> = withContext(Dispatchers.IO) {
val tempVideoList = ArrayList<Video>()
val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

// 🛠️ التعديل: إضافة MediaStore.Video.Media.DATA لضمان جلب مسار الفيديو الفعلي ليكون متواففاً مع كائن الـ Video
val projectionList = mutableListOf(
MediaStore.Video.Media._ID,
MediaStore.Video.Media.TITLE,
MediaStore.Video.Media.DURATION,
MediaStore.Video.Media.SIZE,
MediaStore.Video.Media.DATA
)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
projectionList.add(MediaStore.Video.Media.RESOLUTION)
}

try {
val cursor: Cursor? = context.contentResolver.query(
uri,
projectionList.toTypedArray(),
null,
null,
"${MediaStore.Video.Media.DATE_ADDED} DESC"
)
cursor?.use {
val idCol = it.getColumnIndex(MediaStore.Video.Media._ID)
val titleCol = it.getColumnIndex(MediaStore.Video.Media.TITLE)
val durationCol = it.getColumnIndex(MediaStore.Video.Media.DURATION)
val sizeCol = it.getColumnIndex(MediaStore.Video.Media.SIZE)
val dataCol = it.getColumnIndex(MediaStore.Video.Media.DATA) // 🛠️ قراءة عمود المسار
val resolutionCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) it.getColumnIndex(MediaStore.Video.Media.RESOLUTION) else -1

while (it.moveToNext()) {
val id = if (idCol != -1) it.getLong(idCol) else 0L
val title = if (titleCol != -1) it.getString(titleCol) ?: "Unknown Video" else "Unknown Video"
val duration = if (durationCol != -1) it.getLong(durationCol) else 0L
val size = if (sizeCol != -1) it.getLong(sizeCol) else 0L
val path = if (dataCol != -1) it.getString(dataCol) ?: "" else "" // جلب المسار احتياطاً
val resolution = if (resolutionCol != -1) it.getString(resolutionCol) ?: "Unknown" else "Unknown"
val contentUri = ContentUris.withAppendedId(uri, id)

// 🛠️ بناء كائن الفيديو (تأكد من مطابقة ترتيب البارامترات مع كلاس Video.kt لديك)
// إذا كان كلاس Video يستقبل الـ path، يمكنك تمريره، هنا مررنا الـ contentUri والبيانات الأساسية المتوفرة
tempVideoList.add(
Video(
id = id,
title = title,
duration = duration,
size = size,
resolution = resolution,
uri = contentUri
)
)
}
}
} catch (e: Exception) {
e.printStackTrace()
}
return@withContext tempVideoList
}
}