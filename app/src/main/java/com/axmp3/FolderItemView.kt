package com.axmp3

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class FolderItemView(context: Context) {

val rootView: LinearLayout
val folderIcon: ImageView
val folderNameTxt: TextView
val songCountTxt: TextView

init {
val typedValue = TypedValue()

context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
val textColor = if (typedValue.resourceId != 0) context.getColor(typedValue.resourceId) else Color.CYAN

context.theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)
val subTextColor = if (typedValue.resourceId != 0) context.getColor(typedValue.resourceId) else Color.GRAY

rootView = LinearLayout(context).apply {
layoutParams = ViewGroup.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.WRAP_CONTENT
)
orientation = LinearLayout.HORIZONTAL
gravity = Gravity.CENTER_VERTICAL
setPadding(dpToPx(context, 16), dpToPx(context, 12), dpToPx(context, 16), dpToPx(context, 12))
setBackgroundColor(Color.TRANSPARENT)

val outValue = TypedValue()
context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
isClickable = true
isFocusable = true
// 🛠️ تم التعديل إلى setBackgroundResource لمنع خطأ الـ Unresolved reference
setBackgroundResource(outValue.resourceId)
}

folderIcon = ImageView(context).apply {
layoutParams = LinearLayout.LayoutParams(dpToPx(context, 75), dpToPx(context, 75)).apply {
marginEnd = dpToPx(context, 16)
}
// 🛠️ استخدام آيقونة معرض الصور الافتراضية المضمونة في كل الأنظمة بدلاً من ic_menu_archive
setImageResource(android.R.drawable.ic_menu_gallery)
setColorFilter(Color.parseColor("#FFC107"))
}
rootView.addView(folderIcon)

val textLayout = LinearLayout(context).apply {
layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
orientation = LinearLayout.VERTICAL
}

folderNameTxt = TextView(context).apply {
layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
setTextColor(textColor)
textSize = 16f
typeface = Typeface.DEFAULT_BOLD
maxLines = 1
}
textLayout.addView(folderNameTxt)

songCountTxt = TextView(context).apply {
layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
topMargin = dpToPx(context, 2)
}
setTextColor(subTextColor)
textSize = 13f
maxLines = 1
}
textLayout.addView(songCountTxt)

rootView.addView(textLayout)
}

private fun dpToPx(context: Context, dp: Int): Int {
return (dp * context.resources.displayMetrics.density).toInt()
}
}