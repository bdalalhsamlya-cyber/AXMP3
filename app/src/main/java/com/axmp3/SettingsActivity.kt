package com.axmp3

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView

class SettingsActivity : AppCompatActivity() {

private lateinit var themeRadioGroup: RadioGroup
private lateinit var radioLight: RadioButton
private lateinit var radioDark: RadioButton
private lateinit var radioSystem: RadioButton
private lateinit var preferences: SharedPreferences

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)

// 1. بناء واجهة المستخدم برمجياً
buildLayoutProgrammatically()

// 2. إدارة تفضيلات السمات والوضع الداكن/الفاتح (تم تعديل الاسم ليتطابق مع الـ MainActivity)
preferences = getSharedPreferences("settings", MODE_PRIVATE)
val currentMode = preferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

// تحديد الخيار الحالي بناءً على الإعدادات المحفوظة
when (currentMode) {
AppCompatDelegate.MODE_NIGHT_NO -> radioLight.isChecked = true
AppCompatDelegate.MODE_NIGHT_YES -> radioDark.isChecked = true
else -> radioSystem.isChecked = true
}

// مستمع تغيير الوضع وتطبيقه فورياً
themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
val mode = when (checkedId) {
radioLight.id -> AppCompatDelegate.MODE_NIGHT_NO
radioDark.id -> AppCompatDelegate.MODE_NIGHT_YES
else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}

// تم تصحيح الحفظ هنا لإرسال القيمة الرقمية وقيمة الـ dark_mode معاً لضمان قراءتها في كل مكان
val isDark = (mode == AppCompatDelegate.MODE_NIGHT_YES)
preferences.edit().apply {
putInt("theme_mode", mode)
putBoolean("dark_mode", isDark)
apply()
}

// تطبيق السمة فوراً على كامل التطبيق
AppCompatDelegate.setDefaultNightMode(mode)
}
}

private fun buildLayoutProgrammatically() {
// الحاوية الأساسية للشاشة
val mainLayout = LinearLayout(this).apply {
layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
orientation = LinearLayout.VERTICAL
setBackgroundColor(Color.parseColor("#FF1A1A2E"))
}

// ================= شريط العنوان (Toolbar) =================
val toolbar = RelativeLayout(this).apply {
layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(56))
setBackgroundColor(Color.parseColor("#FF161625"))
setPadding(dpToPx(16), 0, dpToPx(16), 0)
}

val backBtn = ImageView(this).apply {
layoutParams = RelativeLayout.LayoutParams(dpToPx(40), dpToPx(40)).apply {
addRule(RelativeLayout.ALIGN_PARENT_START)
addRule(RelativeLayout.CENTER_VERTICAL)
}
setImageResource(android.R.drawable.ic_menu_revert)
setColorFilter(Color.parseColor("#FFD700"))
setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
isClickable = true
setOnClickListener { finish() }
}
toolbar.addView(backBtn)

val titleTv = TextView(this).apply {
layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
addRule(RelativeLayout.CENTER_IN_PARENT)
}
text = "الإعدادات"
textSize = 20f
setTextColor(Color.WHITE)
typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
}
toolbar.addView(titleTv)
mainLayout.addView(toolbar)

// ================= ScrollView للمحتوى =================
val scrollView = ScrollView(this).apply {
layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
}

val contentLayout = LinearLayout(this).apply {
layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
orientation = LinearLayout.VERTICAL
}

// ================= 1. قسم اختيار ألوان التطبيق (منسدل) =================
val themeCard = CardView(this).apply {
layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
bottomMargin = dpToPx(16)
}
radius = dpToPx(16).toFloat()
setCardBackgroundColor(Color.parseColor("#FF161625"))
cardElevation = 0f
}

val themeContainer = LinearLayout(this).apply {
layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
orientation = LinearLayout.VERTICAL
setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
}

val themeHeaderLayout = LinearLayout(this).apply {
layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
orientation = LinearLayout.HORIZONTAL
gravity = Gravity.CENTER_VERTICAL
isClickable = true
setPadding(0, dpToPx(4), 0, dpToPx(4))
}

val themeTitle = TextView(this).apply {
layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
text = "اختار لون تطبيقك"
textSize = 18f
setTextColor(Color.parseColor("#FFD700"))
typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
}

val themeArrow = ImageView(this).apply {
layoutParams = LinearLayout.LayoutParams(dpToPx(24), dpToPx(24))
setImageResource(android.R.drawable.ic_menu_add)
setColorFilter(Color.parseColor("#FFD700"))
}
themeHeaderLayout.addView(themeTitle)
themeHeaderLayout.addView(themeArrow)
themeContainer.addView(themeHeaderLayout)

themeRadioGroup = RadioGroup(this).apply {
layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
topMargin = dpToPx(12)
}
orientation = RadioGroup.VERTICAL
visibility = View.GONE
}

radioSystem = RadioButton(this).apply {
id = View.generateViewId()
layoutParams = RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
text = "1. لون النظام تلقائيًا"
setTextColor(Color.WHITE)
buttonTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700"))
}
radioLight = RadioButton(this).apply {
id = View.generateViewId()
layoutParams = RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
text = "2. فاتح"
setTextColor(Color.WHITE)
buttonTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700"))
}
radioDark = RadioButton(this).apply {
id = View.generateViewId()
layoutParams = RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
text = "3. داكن"
setTextColor(Color.WHITE)
buttonTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700"))
}

themeRadioGroup.addView(radioSystem)
themeRadioGroup.addView(radioLight)
themeRadioGroup.addView(radioDark)
themeContainer.addView(themeRadioGroup)
themeCard.addView(themeContainer)
contentLayout.addView(themeCard)

themeHeaderLayout.setOnClickListener {
if (themeRadioGroup.visibility == View.GONE) {
themeRadioGroup.visibility = View.VISIBLE
themeArrow.setImageResource(android.R.drawable.ic_menu_revert)
} else {
themeRadioGroup.visibility = View.GONE
themeArrow.setImageResource(android.R.drawable.ic_menu_add)
}
}

// ================= 2. قسم حول المطور (منسدل) =================
val devCard = CardView(this).apply {
layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
bottomMargin = dpToPx(16)
}
radius = dpToPx(16).toFloat()
setCardBackgroundColor(Color.parseColor("#FF161625"))
cardElevation = 0f
}

val devContainer = LinearLayout(this).apply {
layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
orientation = LinearLayout.VERTICAL
setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
}

val devHeaderLayout = LinearLayout(this).apply {
layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
orientation = LinearLayout.HORIZONTAL
gravity = Gravity.CENTER_VERTICAL
isClickable = true
setPadding(0, dpToPx(4), 0, dpToPx(4))
}

val devTitle = TextView(this).apply {
layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
text = "حول المطور"
textSize = 18f
setTextColor(Color.parseColor("#FFD700"))
typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
}

val devArrow = ImageView(this).apply {
layoutParams = LinearLayout.LayoutParams(dpToPx(24), dpToPx(24))
setImageResource(android.R.drawable.ic_menu_add)
setColorFilter(Color.parseColor("#FFD700"))
}
devHeaderLayout.addView(devTitle)
devHeaderLayout.addView(devArrow)
devContainer.addView(devHeaderLayout)

val devDetailsLayout = LinearLayout(this).apply {
layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
topMargin = dpToPx(12)
}
orientation = LinearLayout.VERTICAL
visibility = View.GONE
}

val infoName = createInfoTextView("الاسم: عبد الاله عصام المطحني")
val infoPhone = createInfoTextView("رقم الهاتف: 967780907636")
val infoEmail = createInfoTextView("البريد الإلكتروني: bdalalhsamlya@gmail.com")
val infoTelegram = createInfoTextView("تليجرام: https://t.me/WSTAB_APK").apply {
autoLinkMask = android.text.util.Linkify.WEB_URLS
setLinkTextColor(Color.parseColor("#FFD700"))
linksClickable = true
}

devDetailsLayout.addView(infoName)
devDetailsLayout.addView(infoPhone)
devDetailsLayout.addView(infoEmail)
devDetailsLayout.addView(infoTelegram)
devContainer.addView(devDetailsLayout)
devCard.addView(devContainer)
contentLayout.addView(devCard)

devHeaderLayout.setOnClickListener {
if (devDetailsLayout.visibility == View.GONE) {
devDetailsLayout.visibility = View.VISIBLE
devArrow.setImageResource(android.R.drawable.ic_menu_revert)
} else {
devDetailsLayout.visibility = View.GONE
devArrow.setImageResource(android.R.drawable.ic_menu_add)
}
}

scrollView.addView(contentLayout)
mainLayout.addView(scrollView)
setContentView(mainLayout)
}

private fun createInfoTextView(infoText: String): TextView {
return TextView(this).apply {
layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
bottomMargin = dpToPx(8)
}
text = infoText
setTextColor(Color.WHITE)
textSize = 14f
}
}

private fun dpToPx(dp: Int): Int {
return (dp * resources.displayMetrics.density).toInt()
}
}