package com.axmp3

import android.content.Context
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
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
import java.util.Locale

class SongAdapter(
    private val context: Context,
    private var songList: MutableList<Song>,
    private val listener: OnSongClickListener
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

  private val fullList: ArrayList<Song> = ArrayList<Song>().apply { addAll(songList) }

  private var selectedPosition: Int = -1

  interface OnSongClickListener {
    fun onSongClick(song: Song, position: Int)
  }

  fun updateList(newList: List<Song>) {
    this.songList.clear()
    this.songList.addAll(newList)
    this.fullList.clear()
    this.fullList.addAll(newList)
    notifyDataSetChanged()
  }

  fun filter(text: String) {
    songList.clear()
    if (text.isEmpty()) {
      songList.addAll(fullList)
    } else {
      val query = text.lowercase(Locale.getDefault()).trim()
      for (song in fullList) {
        if (song.title.lowercase(Locale.getDefault()).contains(query) ||
            song.artist.lowercase(Locale.getDefault()).contains(query)) {
          songList.add(song)
        }
      }
    }
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
    // تثبيت اللون الداكن المخصص لعناصر قائمة AXMP3 ليطابق الخلفية السوداء
    val surfaceColor = Color.parseColor("#1E1E1E")
    val secondaryTextColor = Color.parseColor("#00BCD4")
    val goldColor = Color.parseColor("#FFCC00") // الذهبي المعتمد في الواجهة الرئيسية

    val rootLayout =
        LinearLayout(context).apply {
          orientation = LinearLayout.HORIZONTAL
          gravity = Gravity.CENTER_VERTICAL
          setPadding(dpToPx(12f), dpToPx(10f), dpToPx(12f), dpToPx(10f))

          // إضافة تأثير التموج الافتراضي عند الضغط (Ripple Effect)
          val outValue = TypedValue()
          context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
          setBackgroundResource(outValue.resourceId)

          isClickable = true
          isFocusable = true

          // دمج تأثير التموج فوق الخلفية الدائرية الكبسولية للعنصر
          val shapeDrawable =
              GradientDrawable().apply {
                setColor(surfaceColor)
                cornerRadius = dpToPx(15.0f).toFloat() // حواف دائرية متناسقة
              }
          background = shapeDrawable

          layoutParams =
              RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(70f)).apply {
                setMargins(dpToPx(10f), dpToPx(5f), dpToPx(10f), dpToPx(5f))
              }
        }

    // إطار أيقونة الألبوم (الجهة اليمنى برمجياً)
    val frameLayout =
        FrameLayout(context).apply {
          layoutParams = LinearLayout.LayoutParams(dpToPx(70f), dpToPx(70f))
          setPadding(dpToPx(10.0f), dpToPx(0.0f), dpToPx(10.0f), dpToPx(0.0f))
          background =
              GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setStroke(dpToPx(1.5f), goldColor)
              }
        }

    val albumArt =
        ImageView(context).apply {
          layoutParams =
              FrameLayout.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
          scaleType = ImageView.ScaleType.CENTER_CROP
          setImageResource(android.R.drawable.ic_menu_gallery)
        }
    frameLayout.addView(albumArt)
    rootLayout.addView(frameLayout)

    // حاوية النصوص (العنوان والفنان)
    val textContainer =
        LinearLayout(context).apply {
          layoutParams =
              LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dpToPx(14f)
                marginEnd = dpToPx(14f)
              }
          orientation = LinearLayout.VERTICAL
        }

    val songTitle =
        TextView(context).apply {
          layoutParams =
              LinearLayout.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
          setTextColor(goldColor)
          textSize = 12f
          typeface = Typeface.DEFAULT_BOLD
          maxLines = 1
          ellipsize = TextUtils.TruncateAt.END
          gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }

    val songArtist =
        TextView(context).apply {
          layoutParams =
              LinearLayout.LayoutParams(
                      ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                  .apply { topMargin = dpToPx(3f) }
          setTextColor(secondaryTextColor)
          textSize = 12f
          maxLines = 1
          ellipsize = TextUtils.TruncateAt.END
          gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
    textContainer.addView(songTitle)
    textContainer.addView(songArtist)
    rootLayout.addView(textContainer)

    // أيقونة التشغيل الصغيرة في نهاية العنصر
    val playIcon =
        ImageView(context).apply {
          layoutParams = LinearLayout.LayoutParams(dpToPx(28f), dpToPx(28f))
          setImageResource(android.R.drawable.ic_media_play)
          setColorFilter(goldColor)
        }
    rootLayout.addView(playIcon)

    return SongViewHolder(rootLayout, frameLayout, albumArt, songTitle, songArtist, playIcon)
  }

  override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
    val song = songList[position]
    holder.title.text = song.title
    holder.artist.text = song.artist

    // تحديث حالة العنصر إذا كان هو المختار حالياً
    if (position == selectedPosition) {
      holder.playIcon.setImageResource(android.R.drawable.ic_media_pause)
      holder.title.setTextColor(Color.WHITE) // تمييز النص باللون الأبيض أثناء التشغيل
    } else {
      holder.playIcon.setImageResource(android.R.drawable.ic_media_play)
      holder.setTextColorGold()
    }

    holder.itemView.setOnClickListener {
      // استخدام bindingAdapterPosition لضمان الحصول على الموقع الصحيح آمنًا من التصفية والـ Async
      val currentPos = holder.bindingAdapterPosition
      if (currentPos != RecyclerView.NO_POSITION) {
        val previousSelected = selectedPosition
        selectedPosition = currentPos

        notifyItemChanged(previousSelected)
        notifyItemChanged(selectedPosition)

        listener.onSongClick(song, currentPos)
      }
    }
  }

  override fun getItemCount(): Int = songList.size

  private fun dpToPx(dp: Float): Int = (dp * context.resources.displayMetrics.density).toInt()

  class SongViewHolder(
      itemView: View,
      val frameLayout: FrameLayout,
      val albumArt: ImageView,
      val title: TextView,
      val artist: TextView,
      val playIcon: ImageView
  ) : RecyclerView.ViewHolder(itemView) {

    fun setTextColorGold() {
      title.setTextColor(Color.parseColor("#FFCC00"))
    }
  }
}
