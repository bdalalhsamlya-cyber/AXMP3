package com.axmp3 // تأكد أن حرف p صغير وليس كبيراً

import android.net.Uri
import java.io.Serializable

data class Video(
val id: Long,
val title: String,
val duration: Long = 0,               // قيمة افتراضية لتفادي أخطاء النقص
val size: Long = 0,                   // قيمة افتراضية
val resolution: String = "",          // قيمة افتراضية
val uri: Uri = Uri.EMPTY              // قيمة افتراضية فارغة
) : Serializable                          // لدعم نقل كائن الفيديو بين الشاشات بدون مشاكل