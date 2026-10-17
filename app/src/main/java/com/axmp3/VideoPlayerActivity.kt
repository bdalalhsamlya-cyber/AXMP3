package com.axmp3

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import java.io.File

class VideoPlayerActivity : Activity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null
    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var audioManager: android.media.AudioManager? = null
    private lateinit var overlayText: TextView

    private var isLocked = false
    private var currentSpeedIndex = 1
    private val speeds = floatArrayOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)
    private val speedLabels = arrayOf("0.5x", "1.0x", "1.25x", "1.5x", "2.0x")
    private var isLandscape = true
    private var scaleFactor = 1.0f
    private var isPlaying = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        gestureDetector = GestureDetector(this, this)

        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 4.0f))
                playerView.scaleX = scaleFactor
                playerView.scaleY = scaleFactor
                showOverlay("🔍 التكبير: ${(scaleFactor * 100).toInt()}%")
                return true
            }
        })

        val rootLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.BLACK)
        }

        playerView = PlayerView(this).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        rootLayout.addView(playerView)

        overlayText = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
            setTextColor(Color.WHITE)
            textSize = 18f
            setBackgroundColor(Color.parseColor("#AA111111"))
            setPadding(36, 18, 36, 18)
            visibility = View.GONE
        }
        rootLayout.addView(overlayText)

        // ==================== الشريط العلوي الأول (مطابق للصورة) ====================
        val topBar = LinearLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP).apply {
                setMargins(16, 16, 16, 16)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#77000000"))
            setPadding(10, 10, 10, 10)
        }

        val btnBack = createIconButton("←") { finish() }
        
        val videoUriString = intent.getStringExtra("video_uri") ?: ""
        val fileName = if (videoUriString.isNotEmpty()) File(videoUriString).name else "Video Player"
        
        val txtTitle = TextView(this).apply {
            text = fileName
            setTextColor(Color.WHITE)
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(16, 0, 16, 0)
            }
            isSingleLine = true
        }

        val btnMusicNote = createIconButton("🎵") { showOverlay("مسار الصوت") }
        val btnCard = createIconButton("💳") { showOverlay("إعدادات العرض") }
        val btnHw = createIconButton("HW") { showOverlay("فك التشفير: HW") }
        val btnMenu = createIconButton("⋮") { showOverlay("القائمة الرئيسية") }

        topBar.addView(btnBack)
        topBar.addView(txtTitle)
        topBar.addView(btnMusicNote)
        topBar.addView(btnCard)
        topBar.addView(btnHw)
        topBar.addView(btnMenu)
        rootLayout.addView(topBar)

        // ==================== الشريط العلوي الثاني (الأدوات السريعة تحت العنوان) ====================
        val subTopBar = LinearLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START).apply {
                setMargins(16, 95, 16, 16)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val btnAb = createIconButton("A⇄B") { showOverlay("تكرار A-B") }
        val btnWave = createIconButton("📈") { showOverlay("مخطط الصوت") }
        val btnEqualizer = createIconButton("🎛️") { showOverlay("موازن الصوت") }
        
        // زر السرعة
        val btnSpeed = Button(this).apply {
            text = speedLabels[currentSpeedIndex]
            setTextColor(Color.WHITE)
            textSize = 14f
            setBackgroundColor(Color.parseColor("#3F51B5"))
            setPadding(18, 8, 18, 8)
            setOnClickListener {
                currentSpeedIndex = (currentSpeedIndex + 1) % speeds.size
                player?.playbackParameters = PlaybackParameters(speeds[currentSpeedIndex])
                text = speedLabels[currentSpeedIndex]
                showOverlay("⚡ السرعة: ${speedLabels[currentSpeedIndex]}")
            }
        }

        // زر السماعة بدائرة زرقاء
        val btnHeadphone = createIconButton("🎧") {
            setBackgroundColor(Color.parseColor("#3F51B5"))
            showOverlay("وضع السماعة مفعل")
        }

        val btnArrowRight = createIconButton("›") { showOverlay("المزيد من الخيارات") }

        subTopBar.addView(btnAb)
        subTopBar.addView(btnWave)
        subTopBar.addView(btnEqualizer)
        subTopBar.addView(btnSpeed)
        subTopBar.addView(btnHeadphone)
        subTopBar.addView(btnArrowRight)
        rootLayout.addView(subTopBar)

        // ==================== الشريط السفلي (مطابق تماماً للصورة) ====================
        val bottomBar = LinearLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM).apply {
                setMargins(20, 20, 20, 20)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // زر القفل على اليسار في الأسفل
        val btnLock = createIconButton("🔒") {
            isLocked = !isLocked
            showOverlay(if (isLocked) "🔒 تم القفل" else "🔓 تم إلغاء القفل")
            text = if (isLocked) "🔓" else "🔒"
        }

        // الأزرار الوسطى (السابق، تشغيل/إيقاف مؤقت، التالي)
        val centerControls = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
        }

        val btnPrev = createIconButton("⏮") { showOverlay("الفيديو السابق") }
        val btnPlayPause = createIconButton("⏸") {
            isPlaying = !isPlaying
            if (isPlaying) player?.play() else player?.pause()
            text = if (isPlaying) "⏸" else "▶"
        }
        val btnNext = createIconButton("⏭") { showOverlay("الفيديو التالي") }

        centerControls.addView(btnPrev)
        centerControls.addView(btnPlayPause)
        centerControls.addView(btnNext)

        // زر الأبعاد / تدوير الشاشة على اليمين في الأسفل (أيقونة نافذة/تغيير أبعاد دقيقة)
        val btnAspect = createIconButton("⛶") {
            isLandscape = !isLandscape
            requestedOrientation = if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            showOverlay(if (isLandscape) "📱 وضع أفقي" else "📱 وضع عمودي")
        }

        bottomBar.addView(btnLock)
        bottomBar.addView(centerControls)
        bottomBar.addView(btnAspect)
        rootLayout.addView(bottomBar)

        setContentView(rootLayout)

        playerView.setOnTouchListener { _, event ->
            if (isLocked) return@setOnTouchListener true
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }

        if (videoUriString.isBlank()) {
            Toast.makeText(this, "لم يتم استلام مسار الفيديو", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val videoUri = if (videoUriString.startsWith("/")) Uri.fromFile(File(videoUriString)) else Uri.parse(videoUriString)

        player = ExoPlayer.Builder(this)
            .setLoadControl(DefaultLoadControl())
            .build().also { exoPlayer ->
                playerView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(videoUri)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            }
    }

    private fun createIconButton(label: String, onClick: Button.() -> Unit): Button {
        return Button(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 16f
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(16, 8, 16, 8)
            setOnClickListener {
                setTextColor(Color.parseColor("#00B0FF"))
                onClick()
            }
        }
    }

    private fun showOverlay(text: String) {
        overlayText.text = text
        overlayText.visibility = View.VISIBLE
        overlayText.removeCallbacks(hideOverlayRunnable)
        overlayText.postDelayed(hideOverlayRunnable, 1200)
    }

    private val hideOverlayRunnable = Runnable { overlayText.visibility = View.GONE }

    // إيماءات التمرير (السحب للأعلى يزيد، والسحب للأسفل يخفض)
    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (isLocked) return false
        val viewWidth = playerView.width.toFloat()

        // ملاحظة: في أندرويد، distanceY موجبة عند السحب للأعلى (e1.y - e2.y > 0)
        val isDraggingUp = distanceY > 0

        if (e1.x > viewWidth / 2) {
            // التحكم بالصوت
            val maxVolume = audioManager?.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC) ?: 15
            val currentVol = audioManager?.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) ?: 0
            if (isDraggingUp && currentVol < maxVolume) {
                audioManager?.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, currentVol + 1, 0)
            } else if (!isDraggingUp && currentVol > 0) {
                audioManager?.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, currentVol - 1, 0)
            }
            showOverlay("🔊 الصوت: ${((audioManager?.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) ?: 0) * 100) / maxVolume}%")
        } else {
            // التحكم بالسطوع
            val lp = window.attributes
            var brightness = lp.screenBrightness
            if (brightness < 0) brightness = 0.5f

            if (isDraggingUp) {
                brightness = Math.min(1.0f, brightness + 0.05f)
            } else {
                brightness = Math.max(0.01f, brightness - 0.05f)
            }

            lp.screenBrightness = brightness
            window.attributes = lp
            showOverlay("☀️ السطوع: ${(brightness * 100).toInt()}%")
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        if (isLocked) return false
        val viewWidth = playerView.width.toFloat()
        val currentPosition = player?.currentPosition ?: 0L
        if (e.x > viewWidth / 2) {
            player?.seekTo(currentPosition + 10000)
            showOverlay("⏩ +10 ثوانٍ")
        } else {
            player?.seekTo(Math.max(0, currentPosition - 10000))
            showOverlay("⏪ -10 ثوانٍ")
        }
        return true
    }

    override fun onDown(e: MotionEvent): Boolean = true
    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    override fun onLongPress(e: MotionEvent) {}
    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean = false
    override fun onDoubleTapEvent(e: MotionEvent): Boolean = false

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
