package com.axmp3

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView

class MainViewBuilder(private val context: Context) {

  val rootView = LinearLayout(context)
  val toolbar = LinearLayout(context)
  val menuBtn = ImageButton(context)
  val searchBtn = ImageButton(context)
  val logoTxt = TextView(context)

  // الـ SearchView ممتد وبحواف دائرية كبسولية
  val searchView = SearchView(context)
  private val spaceView = View(context)

  // شريط التنقل العلوي
  val topNav = LinearLayout(context)
  val btnFolders = TextView(context)
  val btnPlaylists = TextView(context)
  val btnSongs = TextView(context)
  val btnVideos = TextView(context)

  val recyclerView = RecyclerView(context)

  // حاوية الـ MiniPlayer السفلي والتصاميم الداخلية لها
  val miniPlayerContainer = FrameLayout(context)
  val miniPlayerLayout = LinearLayout(context)
  val miniPlayerTitle = TextView(context)
  val miniPlayBtn = ImageButton(context)
  val miniNextBtn = ImageButton(context)
  val miniPrevBtn = ImageButton(context)

  fun build(): View {
    // الحاوية الرئيسية
    rootView.apply {
      layoutParams =
          LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(Color.BLACK)
    }

    // 1. شريط الأدوات العلوي (Toolbar)
    toolbar.apply {
      layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(45))
      orientation = LinearLayout.HORIZONTAL
      gravity = Gravity.CENTER_VERTICAL
      setPadding(dpToPx(0), 0, dpToPx(0), 0)
      setBackgroundColor(Color.BLACK)
    }

    // زر المنيو الدائري الأصفر المحاط بإطار أبيض
    menuBtn.apply {
      layoutParams = LinearLayout.LayoutParams(dpToPx(43), dpToPx(43))
      setImageResource(
          android.R.drawable.ic_menu_sort_by_size) // يمكنك استبدالها بأيقونة المنيو الخاصة بك
      setColorFilter(Color.parseColor("#FFC107"))
      background =
          GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setStroke(dpToPx(0.0f), Color.WHITE)
            setColor(Color.BLACK)
          }
    }

    // زر البحث الأيقوني
    searchBtn.apply {
      layoutParams =
          LinearLayout.LayoutParams(dpToPx(40), dpToPx(40)).apply {
            setMargins(dpToPx(0), 0, dpToPx(0), 0)
          }
      setImageResource(android.R.drawable.ic_menu_search)
      setColorFilter(Color.CYAN)
      setBackgroundColor(Color.BLACK)
    }

    // حقل البحث الطويل الكبسولي الممتد في المنتصف
    searchView.apply {
      layoutParams =
          LinearLayout.LayoutParams(600, dpToPx(40), 1f).apply {
            setMargins(dpToPx(0), 0, dpToPx(0), 0)
          }
      queryHint = "ابحث هنا..."
      visibility = View.GONE
      setIconifiedByDefault(false)

      val searchIcon = findViewById<View>(androidx.appcompat.R.id.search_mag_icon)
      searchIcon?.visibility = View.GONE
      findViewById<View>(androidx.appcompat.R.id.search_plate)?.background = null

      background =
          GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(10).toFloat()
            setColor(Color.parseColor("#1A1A1A"))
          }

      val searchTextView = findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
      searchTextView?.apply {
        setTextColor(Color.WHITE)
        setHintTextColor(Color.GRAY)
        textSize = 13f
        gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
      }
    }

    // زر حق ايقومنة اسم التطبيق
    spaceView.apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) }

    // لوجو AXMP3 الكبسولي في اليمين
    logoTxt.apply {
      layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(38))
      text = "AXMP3"
      setTextColor(Color.parseColor("#FFCA28"))
      textSize = 14f
      setTypeface(null, android.graphics.Typeface.BOLD)
      gravity = Gravity.CENTER
      setPadding(dpToPx(20), 0, dpToPx(20), 0)
      background =
          GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(10).toFloat()
            setStroke(dpToPx(8.5f), Color.parseColor("#00BCD4"))
            setColor(Color.TRANSPARENT)
          }
    }

    toolbar.addView(menuBtn)
    toolbar.addView(searchBtn)
    toolbar.addView(searchView)
    toolbar.addView(spaceView)
    toolbar.addView(logoTxt)
    rootView.addView(toolbar)

    // 2. شريط التنقل العلوي (ترتيب العناصر من اليمين لليسار)
    topNav.apply {
      layoutParams =
          LinearLayout.LayoutParams(
                  LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
              .apply { setMargins(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(8)) }
      orientation = LinearLayout.HORIZONTAL
      gravity = Gravity.CENTER_VERTICAL
      setBackgroundColor(Color.BLACK)
    }

    fun createCapsuleDrawable(isSelected: Boolean): GradientDrawable {
      return GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dpToPx(15).toFloat()
        if (isSelected) {
          setStroke(dpToPx(1.5f), Color.parseColor("#FFCC00"))
          setColor(Color.parseColor("#FFCC00")) // خلفية ممتلئة بالذهبي للقسم النشط
        } else {
          setStroke(dpToPx(1.5f), Color.parseColor("#FFCA28"))
          setColor(Color.TRANSPARENT)
        }
      }
    }

    val baseBtnParams =
        LinearLayout.LayoutParams(0, dpToPx(38), 1.2f).apply {
          setMargins(dpToPx(4), 0, dpToPx(4), 0)
        }
    val videoBtnParams =
        LinearLayout.LayoutParams(0, dpToPx(38), 1.2f).apply {
          setMargins(dpToPx(4), 0, dpToPx(4), 0)
        }

    // أولاً: قسم الفيديوهات (في أقصى اليمين)
    btnVideos.apply {
      layoutParams = videoBtnParams
      text = "الفيديوهات"
      setTextColor(Color.WHITE)
      gravity = Gravity.CENTER
      textSize = 12f
      background = createCapsuleDrawable(false)
    }

    // الخط الفاصل الأصفر بجانب الفيديوهات
    val divider =
        View(context).apply {
          layoutParams =
              LinearLayout.LayoutParams(dpToPx(2.0f), dpToPx(20)).apply {
                setMargins(dpToPx(4), 0, dpToPx(4), 0)
              }
          setBackgroundColor(Color.parseColor("#00BCD4"))
        }

    // ثانياً: قسم الأغاني (الموسيقى - النشط بالخط الذهبي)
    btnSongs.apply {
      layoutParams = baseBtnParams
      text = "الأغاني"
      setTextColor(Color.WHITE) // تغيير النص للأسود ليظهر فوق الخلفية الذهبية
      gravity = Gravity.CENTER
      textSize = 12f
      background = createCapsuleDrawable(false)
    }

    // ثالثاً: قوائم التشغيل
    btnPlaylists.apply {
      layoutParams = baseBtnParams
      text = "قوائم التشغيل"
      setTextColor(Color.WHITE)
      gravity = Gravity.CENTER
      textSize = 11f
      background = createCapsuleDrawable(false)
    }

    // رابعاً: المجلدات (في أقصى اليسار)
    btnFolders.apply {
      layoutParams = baseBtnParams
      text = "مجلدات"
      setTextColor(Color.WHITE)
      gravity = Gravity.CENTER
      textSize = 12f
      background = createCapsuleDrawable(false)
    }

    topNav.addView(btnVideos)
    topNav.addView(divider)
    topNav.addView(btnSongs)
    topNav.addView(btnPlaylists)
    topNav.addView(btnFolders)
    rootView.addView(topNav)

    // 3. الـ RecyclerView
    recyclerView.apply {
      layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
    }
    rootView.addView(recyclerView)

    // 4. بناء الـ MiniPlayer السفلي المتوافق مع الصورة تماماً
    miniPlayerContainer.apply {
      layoutParams =
          LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
      background =
          GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            // حواف دائرية علوية فقط كما يظهر في الصورة
            cornerRadii =
                floatArrayOf(
                    dpToPx(5).toFloat(),
                    dpToPx(5).toFloat(), // علوي يسار
                    dpToPx(5).toFloat(),
                    dpToPx(5).toFloat(), // علوي يمين
                    2.5f,
                    2.5f,
                    2.5f,
                    2.5f // سفلي
                    )
            setColor(Color.parseColor("#795548")) // لون الخلفية الداكن للمشغل
          }
      setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5))
    }

    miniPlayerLayout.apply {
      layoutParams =
          FrameLayout.LayoutParams(
              FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
      orientation = LinearLayout.HORIZONTAL
      gravity = Gravity.CENTER_VERTICAL
    }

    // أزرار التحكم في جهة اليسار (التالي والسابق)
    miniPrevBtn.apply {
      layoutParams = LinearLayout.LayoutParams(dpToPx(32), dpToPx(32))
      setImageResource(android.R.drawable.ic_media_previous)
      setColorFilter(Color.WHITE)
      setBackgroundColor(Color.TRANSPARENT)
    }

    miniNextBtn.apply {
      layoutParams =
          LinearLayout.LayoutParams(dpToPx(32), dpToPx(32)).apply {
            setMargins(dpToPx(12), 0, 0, 0)
          }
      setImageResource(android.R.drawable.ic_media_next)
      setColorFilter(Color.WHITE)
      setBackgroundColor(Color.TRANSPARENT)
    }

    // عنوان الأغنية في المنتصف
    miniPlayerTitle.apply {
      layoutParams =
          LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            setMargins(dpToPx(12), 0, dpToPx(12), 0)
          }
      text = "لا توجد أغنية قيد التشغيل"
      setTextColor(Color.WHITE)
      textSize = 14f
      gravity = Gravity.CENTER
    }

    // زر التشغيل الرئيسي في أقصى اليمين (مثل المثلث الكبير بالصورة)
    miniPlayBtn.apply {
      layoutParams = LinearLayout.LayoutParams(dpToPx(38), dpToPx(38))
      setImageResource(android.R.drawable.ic_media_play)
      setColorFilter(Color.WHITE)
      setBackgroundColor(Color.TRANSPARENT)
    }

    // إضافة عناصر المشغل بالترتيب الهندسي من اليسار لليمين داخل الـ Horizontal Layout
    miniPlayerLayout.addView(miniPrevBtn)
    miniPlayerLayout.addView(miniNextBtn)
    miniPlayerLayout.addView(miniPlayerTitle)
    miniPlayerLayout.addView(miniPlayBtn)

    miniPlayerContainer.addView(miniPlayerLayout)
    rootView.addView(miniPlayerContainer)

    // التحكم في البحث برمجياً لجعل الـ SearchView يأخذ المساحة بالكامل
    searchBtn.setOnClickListener {
      if (searchView.visibility == View.GONE) {
        searchView.visibility = View.VISIBLE
        spaceView.visibility = View.GONE
        logoTxt.visibility = View.GONE // إخفاء اللوجو مؤقتاً ليعطي مساحة كاملة للبحث
      } else {
        searchView.visibility = View.GONE
        spaceView.visibility = View.VISIBLE
        logoTxt.visibility = View.VISIBLE
      }
    }

    return rootView
  }

  private fun dpToPx(dp: Float): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density).toInt()
  }

  private fun dpToPx(dp: Int): Int {
    return dpToPx(dp.toFloat())
  }
}
