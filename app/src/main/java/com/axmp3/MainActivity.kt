package com.axmp3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.ArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity :
    AppCompatActivity(), SongAdapter.OnSongClickListener, FolderAdapter.OnFolderClickListener {

  private val PERMISSION_REQUEST = 100
  private lateinit var viewBuilder: MainViewBuilder
  private lateinit var mediaLoader: MediaLoader
  private lateinit var miniPlayerManager: MiniPlayerManager

  private val songList = ArrayList<Song>()
  private val videoList = ArrayList<Video>()
  private val displayedSongList = ArrayList<Song>()

  private var adapter: SongAdapter? = null
  private var videoAdapter: VideoAdapter? = null
  private var folderAdapter: FolderAdapter? = null

  private var isShowingVideos = false
  private var isShowingFolders = false

  override fun onCreate(savedInstanceState: Bundle?) {
    val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
    val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
    val targetMode =
        if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    AppCompatDelegate.setDefaultNightMode(targetMode)

    super.onCreate(savedInstanceState)

    viewBuilder = MainViewBuilder(this)
    setContentView(viewBuilder.build())

    mediaLoader = MediaLoader(this)

    miniPlayerManager =
        MiniPlayerManager(this, viewBuilder) { index ->
          try {
            if (index in 0 until displayedSongList.size) {
              val songToPlay = displayedSongList[index]
              
              playSongViaService(songToPlay)

              val intent =
                  Intent(this, NowPlayingActivity::class.java).apply {
                    putExtra("song", songToPlay)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                  }
              startActivity(intent)
            }
          } catch (e: Exception) {
            Toast.makeText(this, "خطأ في فتح المشغل", Toast.LENGTH_SHORT).show()
          }
        }

    viewBuilder.recyclerView.apply {
      setItemViewCacheSize(30)
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(this@MainActivity)
    }

    viewBuilder.menuBtn.setOnClickListener {
      startActivity(Intent(this, SettingsActivity::class.java))
    }

    viewBuilder.searchBtn.setOnClickListener {
      viewBuilder.searchView.visibility =
          if (viewBuilder.searchView.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    viewBuilder.searchView.setOnQueryTextListener(
        object : SearchView.OnQueryTextListener {
          override fun onQueryTextSubmit(query: String?): Boolean = false

          override fun onQueryTextChange(newText: String?): Boolean {
            val queryText = newText ?: ""
            when {
              isShowingVideos -> videoAdapter?.filter(queryText)
              isShowingFolders -> folderAdapter?.filter(queryText)
              else -> adapter?.filter(queryText)
            }
            return true
          }
        })

    viewBuilder.btnSongs.setOnClickListener {
      isShowingVideos = false
      isShowingFolders = false
      viewBuilder.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
      displayedSongList.clear()
      displayedSongList.addAll(songList)
      miniPlayerManager.setSongList(displayedSongList)
      displaySongs()

      viewBuilder.btnSongs.setTextColor(android.graphics.Color.CYAN)
      viewBuilder.btnPlaylists.setTextColor(android.graphics.Color.GRAY)
      viewBuilder.btnVideos.setTextColor(android.graphics.Color.GRAY)
      viewBuilder.btnFolders.setTextColor(android.graphics.Color.GRAY)
    }

    viewBuilder.btnPlaylists.setOnClickListener {
      Toast.makeText(this, "قريباً.. قوائم التشغيل", Toast.LENGTH_SHORT).show()
      viewBuilder.btnSongs.setTextColor(android.graphics.Color.GRAY)
      viewBuilder.btnPlaylists.setTextColor(android.graphics.Color.CYAN)
      viewBuilder.btnVideos.setTextColor(android.graphics.Color.GRAY)
      viewBuilder.btnFolders.setTextColor(android.graphics.Color.GRAY)
    }

    viewBuilder.btnVideos.setOnClickListener {
      isShowingVideos = true
      isShowingFolders = false
      viewBuilder.recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 2)
      displayVideos()

      viewBuilder.btnSongs.setTextColor(android.graphics.Color.GRAY)
      viewBuilder.btnPlaylists.setTextColor(android.graphics.Color.GRAY)
      viewBuilder.btnVideos.setTextColor(android.graphics.Color.CYAN)
      viewBuilder.btnFolders.setTextColor(android.graphics.Color.GRAY)
    }

    viewBuilder.btnFolders.setOnClickListener {
      isShowingVideos = false
      isShowingFolders = true
      viewBuilder.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
      displayFolders()

      viewBuilder.btnSongs.setTextColor(android.graphics.Color.GRAY)
      viewBuilder.btnPlaylists.setTextColor(android.graphics.Color.GRAY)
      viewBuilder.btnVideos.setTextColor(android.graphics.Color.GRAY)
      viewBuilder.btnFolders.setTextColor(android.graphics.Color.CYAN)
    }

    checkPermissions()
  }

  private fun checkPermissions() {
    val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
          arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    if (permissions.any {
      ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }) {
      ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST)
    } else {
      startLoadingData()
    }
  }

  // ⚠️ إضافة دالة معالجة نتيجة الصلاحيات لمنع توقف التطبيق عند منح الإذن
  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<out String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == PERMISSION_REQUEST) {
      if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
        startLoadingData()
      } else {
        Toast.makeText(this, "يجب منح الصلاحيات لتشغيل الموسيقى والفيديوهات", Toast.LENGTH_LONG).show()
      }
    }
  }

  private fun startLoadingData() {
    // استخدام Coroutine مباشر بدلاً من تداخل Thread مع runBlocking
    CoroutineScope(Dispatchers.IO).launch {
      val songs = mediaLoader.loadSongs()
      val videos = mediaLoader.loadVideos()

      withContext(Dispatchers.Main) {
        songList.clear()
        songList.addAll(songs)
        displayedSongList.clear()
        displayedSongList.addAll(songs)

        miniPlayerManager.setSongList(displayedSongList)
        displaySongs()

        videoList.clear()
        videoList.addAll(videos)
      }
    }
  }

  private fun displaySongs() {
    adapter = SongAdapter(this, displayedSongList, this)
    viewBuilder.recyclerView.adapter = adapter
  }

  private fun displayVideos() {
    videoAdapter = VideoAdapter(this, videoList)
    viewBuilder.recyclerView.adapter = videoAdapter
  }

  private fun displayFolders() {
    val foldersMap =
        songList.groupBy { song -> if (!song.artist.isNullOrEmpty()) song.artist else "مجلد عام" }
    val folderModelsList = foldersMap.map { FolderModel(it.key, it.value) }
    folderAdapter = FolderAdapter(this, folderModelsList, this)
    viewBuilder.recyclerView.adapter = folderAdapter
  }

  override fun onFolderClick(folderName: String, songs: List<Song>) {
    isShowingFolders = false
    displayedSongList.clear()
    displayedSongList.addAll(songs)
    miniPlayerManager.setSongList(displayedSongList)
    displaySongs()
  }

  private fun playSongViaService(song: Song) {
    val serviceIntent = Intent(this, MusicService::class.java).apply {
      putExtra("song", song)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startForegroundService(serviceIntent)
    } else {
      startService(serviceIntent)
    }
  }

  override fun onSongClick(song: Song, position: Int) {
    miniPlayerManager.setSongList(displayedSongList)
    playSongViaService(song)

    try {
      val intent =
          Intent(this, NowPlayingActivity::class.java).apply { 
            putExtra("song", song) 
          }
      startActivity(intent)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    miniPlayerManager.unbind()
  }
}
