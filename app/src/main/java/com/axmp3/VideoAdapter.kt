package com.axmp3

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.ArrayList
import java.util.Locale

class VideoAdapter(private val context: Context, private val videoList: MutableList<Video>) :
RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

private val fullList = ArrayList<Video>(videoList)

fun updateList(newList: List<Video>) {
this.videoList.clear()
this.videoList.addAll(newList)
this.fullList.clear()
this.fullList.addAll(newList)
notifyDataSetChanged()
}

fun filter(text: String) {
videoList.clear()
if (text.isEmpty()) {
videoList.addAll(fullList)
} else {
val query = text.lowercase(Locale.getDefault()).trim()
for (video in fullList) {
if (video.title.lowercase(Locale.getDefault()).contains(query)) {
videoList.add(video)
}
}
}
notifyDataSetChanged()
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
// الحاوية الرئيسية للعنصر مع إضافة لمسة التفاعل والحواف الدائرية
val rootLayout = LinearLayout(context).apply {
orientation = LinearLayout.VERTICAL
setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
isClickable = true
isFocusable = true

// إضافة التموج المائي عند النقر فوق العنصر
val outValue = TypedValue()
context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)

background = GradientDrawable().apply {
setColor(Color.parseColor("#FF1A1A2E")) // خلفية داكنة متناسقة
cornerRadius = dpToPx(14).toFloat()
}

layoutParams = RecyclerView.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.WRAP_CONTENT
).apply {
setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
}
}

// الحاوية المحيطة بالصورة المصغرة مع الخط الأصفر الفخم
val cardContainer = FrameLayout(context).apply {
layoutParams = LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
dpToPx(100)
)
setPadding(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3))
background = GradientDrawable().apply {
shape = GradientDrawable.RECTANGLE
cornerRadius = dpToPx(14).toFloat() // جعل الحافة متناسقة تماماً مع الكارد
setStroke(dpToPx(1.5f), Color.parseColor("#FFD700")) // الخط الأصفر المطلوب بدون أخطاء
}
}

val cardView = CardView(context).apply {
layoutParams = FrameLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.MATCH_PARENT
)
setCardBackgroundColor(Color.parseColor("#2A2A35"))
radius = dpToPx(12).toFloat()
cardElevation = 0f
}

val thumbnail = ImageView(context).apply {
id = View.generateViewId()
layoutParams = ViewGroup.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.MATCH_PARENT
)
scaleType = ImageView.ScaleType.CENTER_CROP
}
cardView.addView(thumbnail)
cardContainer.addView(cardView)
rootLayout.addView(cardContainer)

// عنوان الفيديو الفخم باللون الأصفر الذهبي ليتماشى مع الأغاني
val title = TextView(context).apply {
id = View.generateViewId()
layoutParams = LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.WRAP_CONTENT
).apply {
setMargins(dpToPx(4), dpToPx(8), dpToPx(4), 0)
}
ellipsize = TextUtils.TruncateAt.END
maxLines = 1
setTextColor(Color.parseColor("#FFD700")) // تعديل للذهبي الفخم ليتناسق مع مشغل الصوت
textSize = 13.5f
typeface = Typeface.DEFAULT_BOLD
textDirection = View.TEXT_DIRECTION_LOCALE
}
rootLayout.addView(title)

// سطر تفاصيل الفيديو الأفقي
val detailsContainer = LinearLayout(context).apply {
layoutParams = LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.WRAP_CONTENT
).apply {
setMargins(dpToPx(4), dpToPx(6), dpToPx(4), dpToPx(2))
}
orientation = LinearLayout.HORIZONTAL
gravity = Gravity.CENTER_VERTICAL
}

val duration = TextView(context).apply {
id = View.generateViewId()
layoutParams = LinearLayout.LayoutParams(
ViewGroup.LayoutParams.WRAP_CONTENT,
ViewGroup.LayoutParams.WRAP_CONTENT
).apply {
marginEnd = dpToPx(8)
}
setTextColor(Color.parseColor("#BBBBBB"))
textSize = 11f
}

val size = TextView(context).apply {
id = View.generateViewId()
layoutParams = LinearLayout.LayoutParams(
ViewGroup.LayoutParams.WRAP_CONTENT,
ViewGroup.LayoutParams.WRAP_CONTENT
).apply {
marginEnd = dpToPx(8)
}
setTextColor(Color.parseColor("#888888"))
textSize = 11f
}

// كبسولة برمجية صغيرة ومميزة لعرض دقة الفيديو (مثال: 1080p)
val details = TextView(context).apply {
id = View.generateViewId()
layoutParams = LinearLayout.LayoutParams(
ViewGroup.LayoutParams.WRAP_CONTENT,
ViewGroup.LayoutParams.WRAP_CONTENT
)
setPadding(dpToPx(4), dpToPx(1), dpToPx(4), dpToPx(1))
setTextColor(Color.parseColor("#FFFFFF"))
textSize = 9f
typeface = Typeface.DEFAULT_BOLD
background = GradientDrawable().apply {
shape = GradientDrawable.RECTANGLE
cornerRadius = dpToPx(4).toFloat()
setColor(Color.parseColor("#33FFD700")) // خلفية ذهبية شفافة وخفيفة
}
}

detailsContainer.addView(duration)
detailsContainer.addView(size)
detailsContainer.addView(details)
rootLayout.addView(detailsContainer)

return VideoViewHolder(rootLayout, thumbnail, title, details, duration, size)
}

override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
val video = videoList[position]

holder.title.text = video.title
holder.details.text = String.format(Locale.US, "%sp", video.resolution)

val seconds = (video.duration / 1000) % 60
val minutes = (video.duration / (1000 * 60)) % 60
holder.duration.text = String.format(Locale.US, "%02d:%02d", minutes, seconds)

val sizeInMB = video.size / (1024f * 1024f)
holder.size.text = String.format(Locale.US, "%.1f MB", sizeInMB)

Glide.with(context)
.asBitmap()
.load(video.uri)
.placeholder(android.R.color.darker_gray)
.error(android.R.color.darker_gray)
.skipMemoryCache(false)
.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
.override(240, 240) // تحسين جودة العرض المصغر قليلاً
.centerCrop()
.into(holder.thumbnail)

holder.itemView.setOnClickListener {
val intent = Intent(context, VideoPlayerActivity::class.java).apply {
putExtra("video_uri", video.uri.toString())
}
context.startActivity(intent)
}
}

override fun getItemCount(): Int = videoList.size

private fun dpToPx(dp: Int): Int {
return (dp * context.resources.displayMetrics.density).toInt()
}

// إضافة دالة إضافية تدعم الـ Float لحل خطأ الـ Compiler
private fun dpToPx(dp: Float): Int {
return (dp * context.resources.displayMetrics.density).toInt()
}

class VideoViewHolder(
itemView: View,
val thumbnail: ImageView,
val title: TextView,
val details: TextView,
val duration: TextView,
val size: TextView
) : RecyclerView.ViewHolder(itemView)
}