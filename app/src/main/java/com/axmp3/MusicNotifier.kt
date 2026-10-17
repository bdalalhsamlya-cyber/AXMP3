package com.axmp3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

class MusicNotifier(private val context: Context) {

private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

companion object {
const val CHANNEL_ID = "music_channel" // تم توحيده ليطابق المعرف في MusicService
const val NOTIFICATION_ID = 1001

// 🛠️ تم تعديل المعرفات هنا لتطابق قيم الـ actions الموجودة في الـ MusicService تماماً
const val ACTION_PREVIOUS = "com.axmp3.ACTION_PREVIOUS"
const val ACTION_PLAY_PAUSE = "com.axmp3.ACTION_PLAY_PAUSE" // تطابق ACTION_TOGGLE في السيرفس
const val ACTION_NEXT = "com.axmp3.ACTION_NEXT"
}

init {
createNotificationChannel()
}

private fun createNotificationChannel() {
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
val channel = NotificationChannel(
CHANNEL_ID,
"مشغل الموسيقى",
NotificationManager.IMPORTANCE_LOW
).apply {
description = "إشعارات التحكم في تشغيل الموسيقى"
setShowBadge(false)
}
notificationManager.createNotificationChannel(channel)
}
}

private fun getAlbumArt(uriString: String?): Bitmap? {
return try {
if (uriString != null) {
val uri = Uri.parse(uriString)
val inputStream = context.contentResolver.openInputStream(uri)
val bitmap = BitmapFactory.decodeStream(inputStream)
inputStream?.close()
bitmap
} else {
null
}
} catch (e: Exception) {
null
}
}

fun showNotification(song: Song, isPlaying: Boolean) {
val prevIntent = Intent(context, NotifyReceiver::class.java).setAction(ACTION_PREVIOUS)
val prevPendingIntent = PendingIntent.getBroadcast(context, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

val playIntent = Intent(context, NotifyReceiver::class.java).setAction(ACTION_PLAY_PAUSE)
val playPendingIntent = PendingIntent.getBroadcast(context, 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

val nextIntent = Intent(context, NotifyReceiver::class.java).setAction(ACTION_NEXT)
val nextPendingIntent = PendingIntent.getBroadcast(context, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

val clickIntent = Intent(context, NowPlayingActivity::class.java)
val clickPendingIntent = PendingIntent.getActivity(context, 3, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

val playIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

val albumArt = getAlbumArt(song.uri?.toString())
val largeIconBitmap = albumArt ?: BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_media_play)

val notification = NotificationCompat.Builder(context, CHANNEL_ID)
.setSmallIcon(android.R.drawable.ic_media_play)
.setContentTitle(song.title)
.setContentText(song.artist)
.setLargeIcon(largeIconBitmap)
.setContentIntent(clickPendingIntent)
.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
.setPriority(NotificationCompat.PRIORITY_LOW)
.setOngoing(isPlaying)

.addAction(android.R.drawable.ic_media_previous, "السابق", prevPendingIntent)
.addAction(playIcon, "تشغيل/إيقاف", playPendingIntent)
.addAction(android.R.drawable.ic_media_next, "التالي", nextPendingIntent)

.setStyle(
androidx.media.app.NotificationCompat.MediaStyle()
.setShowActionsInCompactView(0, 1, 2)
)
.build()

notificationManager.notify(NOTIFICATION_ID, notification)
}

fun cancelNotification() {
notificationManager.cancel(NOTIFICATION_ID)
}
}

// ==========================================
// الـ Receiver الموحد والمطابق بالكامل للسيرفس
// ==========================================
class NotifyReceiver : BroadcastReceiver() {
override fun onReceive(context: Context, intent: Intent) {
val serviceIntent = Intent(context, MusicService::class.java)

when (intent.action) {
MusicNotifier.ACTION_PREVIOUS -> {
serviceIntent.action = MusicService.ACTION_PREVIOUS
context.startService(serviceIntent)
}
MusicNotifier.ACTION_PLAY_PAUSE -> {
serviceIntent.action = MusicService.ACTION_TOGGLE // ربط دقيق بـ ACTION_TOGGLE
context.startService(serviceIntent)
}
MusicNotifier.ACTION_NEXT -> {
serviceIntent.action = MusicService.ACTION_NEXT
context.startService(serviceIntent)
}
}
}
}