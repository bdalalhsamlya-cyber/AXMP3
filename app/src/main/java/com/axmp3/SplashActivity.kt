package com.axmp3

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)

// الألوان المتوافقة مع هوية تطبيقك (نفس ألوان واجهة المشغل الحالية)
val backgroundColor = Color.parseColor("#151829")
val accentColor = Color.parseColor("#FFCC00")
val textColor = Color.WHITE

// 1. الحاوية الرئيسية للواجهة
val rootView = RelativeLayout(this).apply {
layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
setBackgroundColor(backgroundColor)
}

// 2. حاوية العناصر بمنتصف الشاشة (الاسم + شريط التحميل)
val centerLayout = LinearLayout(this).apply {
val params = RelativeLayout.LayoutParams(
RelativeLayout.LayoutParams.WRAP_CONTENT,
RelativeLayout.LayoutParams.WRAP_CONTENT
).apply {
addRule(RelativeLayout.CENTER_IN_PARENT)
}
layoutParams = params
orientation = LinearLayout.VERTICAL
gravity = Gravity.CENTER
}

// 3. نص اسم التطبيق مع حركة تكبير وظهور (Scale + Fade)
val appNameText = TextView(this).apply {
layoutParams = LinearLayout.LayoutParams(
LinearLayout.LayoutParams.WRAP_CONTENT,
LinearLayout.LayoutParams.WRAP_CONTENT
).apply {
bottomMargin = (32 * resources.displayMetrics.density).toInt()
}
text = "AXMP3"
setTextColor(textColor)
textSize = 50f
typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
}

// إعداد ميكس من الحركات لنص اسم التطبيق
val animationSet = AnimationSet(true).apply {
// حركة التكبير التدريجي (من 70% إلى 100%)
addAnimation(ScaleAnimation(0.7f, 1.0f, 1.7f, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
duration = 3000
})
// حركة الظهور التدريجي من الشفافية الكاملة
addAnimation(AlphaAnimation(1.0f, 1.0f).apply {
duration = 3000
})
}
appNameText.startAnimation(animationSet)
centerLayout.addView(appNameText)

// 4. مؤشر التحميل الدائري المتحرك (ProgressBar)
val progressBar = ProgressBar(this).apply {
layoutParams = LinearLayout.LayoutParams(
(45 * resources.displayMetrics.density).toInt(),
(45 * resources.displayMetrics.density).toInt()
)
// تلوين مؤشر التحميل بلون الأكسنت للتطبيق
indeterminateDrawable?.setColorFilter(accentColor, android.graphics.PorterDuff.Mode.SRC_IN)
}
centerLayout.addView(progressBar)

rootView.addView(centerLayout)
setContentView(rootView)

// 5. موقت ذكي للانتظار لمدة 3 ثوانٍ (3000 ملي ثانية) ثم فتح الواجهة الرئيسية
Handler(Looper.getMainLooper()).postDelayed({
val intent = Intent(this@SplashActivity, MainActivity::class.java)
startActivity(intent)

// حركة انتقال ناعمة وتدمير واجهة الـ Splash حتى لا يعود المستخدم إليها بالزر الخلفي
finish()
overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}, 3000)
}
}