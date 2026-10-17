package com.axmp3

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.TextUtils
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class NowPlayingActivity : AppCompatActivity(), ServiceConnection {

  private var musicService: MusicService? = null
  private var isBound = false

  private lateinit var albumArt: ImageView
  private lateinit var playPauseBtn: ImageView
  private lateinit var nextBtn: ImageView
  private lateinit var prevBtn: ImageView
  private lateinit var titleText: TextView
  private lateinit var artistText: TextView
  private lateinit var currentTime: TextView
  private lateinit var totalTime: TextView
  private lateinit var seekBar: SeekBar
  private lateinit var btnBack: ImageView
  private lateinit var rootView: RelativeLayout

  private val handler = Handler(Looper.getMainLooper())
  private var updateSeekBar: Runnable? = null
  private lateinit var rotateAnimation: RotateAnimation
  private lateinit var gestureDetector: GestureDetector

  private var startY: Float = 0f
  private var isDragging = false

  // متغير لحفظ المعرف الخاص بالأغنية الحالية لمنع تكرار تحديث الواجهة بدون داعي
  private var lastSongId: Long = -1L

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // 1. بناء التصميم برمجياً وتطبيق نظام الألوان ديناميكياً ليطابق الصورة تماماً
    buildLayoutProgrammatically()

    // 2. ربط الخدمة الخلفية للموسيقى فوراً
    val serviceIntent = Intent(this, MusicService::class.java)
    bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)

    // 3. إعداد حركة دوران الصورة المصغرة (CD Layout Effect)
    rotateAnimation =
        RotateAnimation(
                0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            .apply {
              interpolator = LinearInterpolator()
              duration = 20000
              repeatCount = Animation.INFINITE
            }

    // المستمعات للضغط
    btnBack.setOnClickListener { finish() }

    playPauseBtn.setOnClickListener {
      musicService?.let {
        it.togglePlayPause()
        updatePlayButton()
      }
    }

    nextBtn.setOnClickListener { playNext() }
    prevBtn.setOnClickListener { playPrevious() }

    seekBar.setOnSeekBarChangeListener(
        object : SeekBar.OnSeekBarChangeListener {
          override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
              musicService?.seekTo(progress)
            }
          }

          override fun onStartTrackingTouch(seekBar: SeekBar?) {}

          override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    // إعداد السحب الأفقي (Fling) لقلب الأغاني بشكل متوافق تماماً
    gestureDetector =
        GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
              override fun onFling(
                  e1: MotionEvent?,
                  e2: MotionEvent,
                  velocityX: Float,
                  velocityY: Float
              ): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                if (Math.abs(diffX) > Math.abs(diffY) * 1.5f) {
                  if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    if (diffX > 0) playPrevious() else playNext()
                    return true
                  }
                }
                return false
              }
            })

    // إعداد السحب العمودي لإغلاق الواجهة برمجياً
    rootView.setOnTouchListener { _, event ->
      gestureDetector.onTouchEvent(event)
      switchTouchActions(event)
    }
  }

  private fun switchTouchActions(event: MotionEvent): Boolean {
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        startY = event.y
        isDragging = false
      }
      MotionEvent.ACTION_MOVE -> {
        val deltaY = event.y - startY
        if (Math.abs(deltaY) > 20 && !isDragging) {
          isDragging = true
        }
        if (isDragging) {
          rootView.translationY = deltaY
          return true
        }
      }
      MotionEvent.ACTION_UP -> {
        if (isDragging) {
          val finalDelta = event.y - startY
          if (Math.abs(finalDelta) > 300) {
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
          } else {
            rootView.animate().translationY(0f).setDuration(200).start()
          }
          isDragging = false
          return true
        }
      }
    }
    return true
  }

  private fun buildLayoutProgrammatically() {
    val backgroundColor = "#151829"
    val accentColor = Color.parseColor("#FFCC00")
    val textColor = Color.WHITE
    val subTextColor = Color.parseColor("#A0A5BA")

    rootView =
        RelativeLayout(this).apply {
          layoutParams =
              ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
          setBackgroundColor(Color.parseColor(backgroundColor))
          setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(32))
        }

    btnBack =
        ImageView(this).apply {
          id = View.generateViewId()
          val params =
              RelativeLayout.LayoutParams(dpToPx(35), dpToPx(35)).apply {
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
                addRule(RelativeLayout.ALIGN_PARENT_END)
              }
          layoutParams = params
          setImageResource(android.R.drawable.ic_menu_revert)
          setColorFilter(accentColor)
        }
    rootView.addView(btnBack)

    val controlsLayout =
        LinearLayout(this).apply {
          id = View.generateViewId()
          val params =
              RelativeLayout.LayoutParams(
                      RelativeLayout.LayoutParams.MATCH_PARENT,
                      RelativeLayout.LayoutParams.WRAP_CONTENT)
                  .apply {
                    addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    bottomMargin = dpToPx(24)
                  }
          layoutParams = params
          gravity = Gravity.CENTER
          orientation = LinearLayout.HORIZONTAL
        }

    prevBtn =
        ImageView(this).apply {
          layoutParams =
              LinearLayout.LayoutParams(dpToPx(55), dpToPx(55)).apply { marginEnd = dpToPx(32) }
          setImageResource(android.R.drawable.ic_media_previous)
          setColorFilter(accentColor)
        }
    playPauseBtn =
        ImageView(this).apply {
          layoutParams = LinearLayout.LayoutParams(dpToPx(75), dpToPx(75))
          setImageResource(android.R.drawable.ic_media_play)
          setColorFilter(accentColor)
        }
    nextBtn =
        ImageView(this).apply {
          layoutParams =
              LinearLayout.LayoutParams(dpToPx(55), dpToPx(55)).apply { marginStart = dpToPx(32) }
          setImageResource(android.R.drawable.ic_media_next)
          setColorFilter(accentColor)
        }

    controlsLayout.addView(prevBtn)
    controlsLayout.addView(playPauseBtn)
    controlsLayout.addView(nextBtn)
    rootView.addView(controlsLayout)

    currentTime =
        TextView(this).apply {
          id = View.generateViewId()
          val params =
              RelativeLayout.LayoutParams(
                      ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                  .apply {
                    addRule(RelativeLayout.ABOVE, controlsLayout.id)
                    addRule(RelativeLayout.ALIGN_PARENT_START)
                    marginStart = dpToPx(12)
                    bottomMargin = dpToPx(16)
                  }
          layoutParams = params
          setTextColor(accentColor)
          textSize = 12f
          text = "00:00"
        }
    rootView.addView(currentTime)

    totalTime =
        TextView(this).apply {
          id = View.generateViewId()
          val params =
              RelativeLayout.LayoutParams(
                      ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                  .apply {
                    addRule(RelativeLayout.ABOVE, controlsLayout.id)
                    addRule(RelativeLayout.ALIGN_PARENT_END)
                    marginEnd = dpToPx(12)
                    bottomMargin = dpToPx(16)
                  }
          layoutParams = params
          setTextColor(accentColor)
          textSize = 12f
          text = "00:00"
        }
    rootView.addView(totalTime)

    seekBar =
        SeekBar(this).apply {
          id = View.generateViewId()
          val params =
              RelativeLayout.LayoutParams(
                      RelativeLayout.LayoutParams.MATCH_PARENT,
                      RelativeLayout.LayoutParams.WRAP_CONTENT)
                  .apply {
                    addRule(RelativeLayout.ABOVE, currentTime.id)
                    bottomMargin = dpToPx(12)
                  }
          layoutParams = params
          progressDrawable.setColorFilter(accentColor, android.graphics.PorterDuff.Mode.SRC_IN)
          thumb.setColorFilter(accentColor, android.graphics.PorterDuff.Mode.SRC_IN)
        }
    rootView.addView(seekBar)

    artistText =
        TextView(this).apply {
          id = View.generateViewId()
          val params =
              RelativeLayout.LayoutParams(
                      RelativeLayout.LayoutParams.MATCH_PARENT,
                      RelativeLayout.LayoutParams.WRAP_CONTENT)
                  .apply {
                    addRule(RelativeLayout.ABOVE, seekBar.id)
                    bottomMargin = dpToPx(24)
                  }
          layoutParams = params
          gravity = Gravity.CENTER
          setTextColor(subTextColor)
          textSize = 13f
        }
    rootView.addView(artistText)

    titleText =
        TextView(this).apply {
          id = View.generateViewId()
          val params =
              RelativeLayout.LayoutParams(
                      RelativeLayout.LayoutParams.MATCH_PARENT,
                      RelativeLayout.LayoutParams.WRAP_CONTENT)
                  .apply {
                    addRule(RelativeLayout.ABOVE, artistText.id)
                    bottomMargin = dpToPx(8)
                  }
          layoutParams = params
          gravity = Gravity.CENTER
          setTextColor(textColor)
          textSize = 17f
          typeface = Typeface.DEFAULT_BOLD
          maxLines = 1
          ellipsize = TextUtils.TruncateAt.END
        }
    rootView.addView(titleText)

    albumArt =
        ImageView(this).apply {
          id = View.generateViewId()
          val params =
              RelativeLayout.LayoutParams(dpToPx(240), dpToPx(240)).apply {
                addRule(RelativeLayout.CENTER_HORIZONTAL)
                addRule(RelativeLayout.BELOW, btnBack.id)
                addRule(RelativeLayout.ABOVE, titleText.id)
                topMargin = dpToPx(30)
                bottomMargin = dpToPx(30)
              }
          layoutParams = params
          scaleType = ImageView.ScaleType.CENTER_CROP
          setImageResource(android.R.drawable.ic_menu_gallery)
        }
    rootView.addView(albumArt)

    setContentView(rootView)
  }

  override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
    val binder = service as MusicService.MusicBinder
    val srv = binder.service
    musicService = srv
    isBound = true

    val currentSongInService = srv.getCurrentSong()

    if (currentSongInService != null) {
      updateUI(currentSongInService)
      startSeekBarUpdate()
      updatePlayButton()
    } else {
      handler.postDelayed(
          {
            srv.getCurrentSong()?.let { song ->
              updateUI(song)
              startSeekBarUpdate()
              updatePlayButton()
            }
                ?: run {
                  Toast.makeText(this, "جاري مزامنة تشغيل الموسيقى...", Toast.LENGTH_SHORT).show()
                }
          },
          400)
    }
  }

  override fun onServiceDisconnected(name: ComponentName?) {
    musicService = null
    isBound = false
  }

  private fun updateUI(song: Song) {
    if (lastSongId == song.id) return // إذا كانت نفس الأغنية لا داعي لإعادة معالجة الغلاف
    lastSongId = song.id

    titleText.text = song.title
    val fileSizeMb = song.size / (1024f * 1024f)
    artistText.text = String.format(Locale.getDefault(), "%s | %.2f MB", song.artist, fileSizeMb)

    try {
      // 🛠️ التعديل الجوهري: استخدام الـ uri الخاص بالأغنية بدلاً من الفايل باث المتغير لتجنب مشاكل
      // الصلاحيات في أندرويد 13/14
      if (song.uri != null) {
        val retriever = android.media.MediaMetadataRetriever()
        retriever.setDataSource(this, song.uri)
        val art = retriever.embeddedPicture
        if (art != null) {
          val bitmap = BitmapFactory.decodeByteArray(art, 0, art.size)
          albumArt.setImageBitmap(bitmap)
          setBlurredBackground(bitmap)
        } else {
          setDefaultTheme()
        }
        retriever.release()
      } else {
        setDefaultTheme()
      }
    } catch (e: Exception) {
      setDefaultTheme()
    }
  }

  private fun setDefaultTheme() {
    albumArt.setImageResource(android.R.drawable.ic_menu_gallery)
    albumArt.setColorFilter(Color.parseColor("#FFCC00"), android.graphics.PorterDuff.Mode.SRC_IN)

    rootView.background = null
    rootView.setBackgroundColor(Color.parseColor("#151829"))
    titleText.setTextColor(Color.WHITE)
    artistText.setTextColor(Color.parseColor("#A0A5BA"))
  }

  private fun setBlurredBackground(original: Bitmap) {
    try {
      val scaled = Bitmap.createScaledBitmap(original, 50, 50, true)
      val drawable = BitmapDrawable(resources, scaled)
      rootView.background = drawable
      titleText.setTextColor(Color.WHITE)
      artistText.setTextColor(Color.parseColor("#CCCCCC"))
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun startSeekBarUpdate() {
    updateSeekBar =
        object : Runnable {
          override fun run() {
            musicService?.let {
              // 🛠️ التعديل الجوهري: فحص ما إذا انتقل السيرفس تلقائياً للأغنية التالية لتحديث واجهة
              // المستخدم ديناميكياً
              val currentSong = it.getCurrentSong()
              if (currentSong != null && currentSong.id != lastSongId) {
                updateUI(currentSong)
              }

              val pos = it.getCurrentPosition()
              val dur = it.getDuration()
              seekBar.max = dur
              seekBar.progress = pos
              currentTime.text = formatTime(pos)
              totalTime.text = formatTime(dur)

              // تحديث زر التشغيل آلياً بناءً على حالة السيرفس الحالية الحقيقية
              updatePlayButton()
            }
            handler.postDelayed(this, 500)
          }
        }
    handler.post(updateSeekBar!!)
  }

  private fun updatePlayButton() {
    musicService?.let {
      if (it.isPlaying()) {
        playPauseBtn.setImageResource(android.R.drawable.ic_media_pause)
        if (albumArt.animation == null) {
          albumArt.startAnimation(rotateAnimation)
        }
      } else {
        playPauseBtn.setImageResource(android.R.drawable.ic_media_play)
        albumArt.clearAnimation()
      }
    }
  }

  private fun playNext() {
    musicService?.let {
      it.nextSong()
      handler.postDelayed(
          {
            it.getCurrentSong()?.let { song -> updateUI(song) }
            updatePlayButton()
          },
          150)
    }
  }

  private fun playPrevious() {
    musicService?.let {
      it.previousSong()
      handler.postDelayed(
          {
            it.getCurrentSong()?.let { song -> updateUI(song) }
            updatePlayButton()
          },
          150)
    }
  }

  private fun formatTime(millis: Int): String {
    val sec = millis / 1000
    val min = sec / 60
    return String.format(Locale.getDefault(), "%02d:%02d", min, sec % 60)
  }

  private fun dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
  }

  override fun onDestroy() {
    super.onDestroy()
    if (isBound) {
      unbindService(this)
      isBound = false
    }
    updateSeekBar?.let { handler.removeCallbacks(it) }
  }
}
