package com.axmp3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import java.io.IOException
import java.util.ArrayList

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

private val binder: IBinder = MusicBinder()
private var mediaPlayer: MediaPlayer? = null
private var currentSong: Song? = null
private var isPrepared = false

private var songsList = ArrayList<Song>()
private var currentSongIndex = 0

private lateinit var musicNotifier: MusicNotifier

inner class MusicBinder : Binder() {
val service: MusicService
get() = this@MusicService
}

override fun onCreate() {
super.onCreate()
createNotificationChannel()
musicNotifier = MusicNotifier(this)
initMediaPlayer()
}

private fun initMediaPlayer() {
if (mediaPlayer == null) {
mediaPlayer = MediaPlayer().apply {
// 1. إعطاء صلاحية استمرار العمل عند قفل الشاشة
setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

// 2. ضبط خصائص الصوت (Audio Attributes) لضمان خروج الصوت عبر مكبرات ومخرجات الوسائط الافتراضية
val audioAttributes = AudioAttributes.Builder()
.setUsage(AudioAttributes.USAGE_MEDIA)
.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
.build()
setAudioAttributes(audioAttributes)

setOnPreparedListener(this@MusicService)
setOnCompletionListener(this@MusicService)
setOnErrorListener(this@MusicService)
}
}
}

override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
if (intent != null) {
when (intent.action) {
ACTION_PLAY -> {
val song: Song? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
intent.getParcelableExtra("song", Song::class.java)
} else {
@Suppress("DEPRECATION")
intent.getParcelableExtra("song")
}
song?.let { playSong(it) }
}
ACTION_TOGGLE -> togglePlayPause()
ACTION_NEXT -> nextSong()
ACTION_PREVIOUS -> previousSong()
}
}
return START_STICKY
}

override fun onBind(intent: Intent?): IBinder {
return binder
}

fun setSongsList(list: List<Song>, initialIndex: Int) {
songsList.clear()
songsList.addAll(list)
currentSongIndex = initialIndex
}

fun playSongAt(index: Int) {
if (songsList.isNotEmpty() && index >= 0 && index < songsList.size) {
currentSongIndex = index
playSong(songsList[currentSongIndex])
}
}

fun playSong(song: Song) {
isPrepared = false
currentSong = song

initMediaPlayer()

mediaPlayer?.let { mp ->
try {
mp.reset()

val songUri: Uri? = song.uri
if (songUri != null && songUri != Uri.EMPTY) {
mp.setDataSource(applicationContext, songUri)
} else if (!song.path.isNullOrEmpty()) {
mp.setDataSource(song.path)
} else {
Log.e("MusicService", "مسار ملف الصوت فارغ أو غير صالح")
return
}

mp.prepareAsync()
} catch (e: IOException) {
Log.e("MusicService", "خطأ في تحميل مصدر ملف الصوت", e)
} catch (e: IllegalStateException) {
Log.e("MusicService", "خطأ في حالة كائن الميديا بلاير", e)
}
}
}

override fun onPrepared(mp: MediaPlayer) {
isPrepared = true
mp.start() // ينطلق الصوت هنا فوراً

// تحسين: تفعيل الإشعار وربطه بالـ Foreground لضمان عدم كتم الصوت خلف الواجهة برمجياً
currentSong?.let {
musicNotifier.showNotification(it, isPlaying = true)
}
}

override fun onCompletion(mp: MediaPlayer) {
nextSong()
}

override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
Log.e("MusicService", "حدث خطأ مفاجئ في المشغل: What: $what, Extra: $extra")
mp.reset()
isPrepared = false
return true // تم معالجة الخطأ لمنع الانهيار
}

fun togglePlayPause() {
if (mediaPlayer != null && isPrepared) {
val playingState: Boolean
if (mediaPlayer!!.isPlaying) {
mediaPlayer!!.pause()
playingState = false
} else {
mediaPlayer!!.start()
playingState = true
}
currentSong?.let {
musicNotifier.showNotification(it, isPlaying = playingState)
}
}
}

fun nextSong() {
if (songsList.isNotEmpty()) {
if (currentSongIndex < songsList.size - 1) {
currentSongIndex++
} else {
currentSongIndex = 0
}
playSong(songsList[currentSongIndex])
}
}

fun previousSong() {
if (songsList.isNotEmpty()) {
if (currentSongIndex > 0) {
currentSongIndex--
} else {
currentSongIndex = songsList.size - 1
}
playSong(songsList[currentSongIndex])
}
}

fun isPlaying(): Boolean = mediaPlayer != null && isPrepared && mediaPlayer!!.isPlaying
fun getCurrentPosition(): Int = if (mediaPlayer != null && isPrepared) mediaPlayer!!.currentPosition else 0
fun getDuration(): Int = if (mediaPlayer != null && isPrepared) mediaPlayer!!.duration else 0
fun getCurrentSong(): Song? = currentSong
fun getCurrentIndex(): Int = currentSongIndex

fun seekTo(msec: Int) {
if (mediaPlayer != null && isPrepared) {
mediaPlayer!!.seekTo(msec)
}
}

fun stopMusic() {
mediaPlayer?.let {
try {
if (it.isPlaying) {
it.stop()
}
} catch (e: Exception) {
e.printStackTrace()
}
it.release()
}
mediaPlayer = null
isPrepared = false
musicNotifier.cancelNotification()

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
stopForeground(STOP_FOREGROUND_REMOVE)
} else {
@Suppress("DEPRECATION")
stopForeground(true)
}
stopSelf()
}

private fun createNotificationChannel() {
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
val channel = NotificationChannel(
CHANNEL_ID, "Music Playback", NotificationManager.IMPORTANCE_LOW
).apply {
description = "قناة إشعارات مشغل الصوت لتطبيق AXMP3"
lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
}
val manager = getSystemService(NotificationManager::class.java)
manager?.createNotificationChannel(channel)
}
}

override fun onDestroy() {
super.onDestroy()
mediaPlayer?.let {
try {
if (it.isPlaying) {
it.stop()
}
} catch (e: Exception) {
e.printStackTrace()
}
it.release()
mediaPlayer = null
}
}

companion object {
const val ACTION_PLAY = "PLAY"
const val CHANNEL_ID = "music_channel"

const val ACTION_TOGGLE = "com.axmp3.ACTION_PLAY_PAUSE"
const val ACTION_NEXT = "com.axmp3.ACTION_NEXT"
const val ACTION_PREVIOUS = "com.axmp3.ACTION_PREVIOUS"
}
}