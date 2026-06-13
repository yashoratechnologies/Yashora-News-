package com.surya.yashoranews

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.*
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.media3.common.*
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import com.google.common.util.concurrent.MoreExecutors
import com.bumptech.glide.Glide
import com.surya.yashoranews.Adapter.PodcastAdapter
import com.surya.yashoranews.Utils.LocaleHelper
import com.surya.yashoranews.DataModels.RadioStation
import com.surya.yashoranews.DataModels.Podcast
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var miniBar: View
    private lateinit var fullPlayer: View
    private lateinit var chipGroup: ChipGroup
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var categoryScroll: View
    private lateinit var suggestionRv: RecyclerView
    private lateinit var fullPlayerScrollView: View

    // Seekbar & Time Views
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalDuration: TextView
    private val handler = Handler(Looper.getMainLooper())

    private var miniTitle: TextView? = null
    private var fullTitle: TextView? = null
    private var miniPlayBtn: ImageButton? = null
    private var fullPlayBtn: ImageButton? = null
    private var miniImg: ImageView? = null
    private var fullImg: ImageView? = null

    // Player & Playlist Logic
    private var controller: MediaController? = null
    private var currentCategory: String = "general"
    private var currentPlaylist: List<Podcast> = ArrayList()
    private var currentIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupFloatingAndGestures()
        setupNavigation()
        setupChipLogic()

        miniBar.visibility = View.GONE
        fullPlayer.visibility = View.GONE

        if (savedInstanceState == null) checkFirstTimeUser()
    }

    private fun initViews() {
        miniBar = findViewById(R.id.miniBar)
        fullPlayer = findViewById(R.id.fullPlayerLayout)
        chipGroup = findViewById(R.id.chipGroup)
        bottomNav = findViewById(R.id.bottom_navigation)
        categoryScroll = findViewById(R.id.categoryScroll)
        fullPlayerScrollView = findViewById(R.id.fullPlayerScrollView)

        miniTitle = findViewById(R.id.miniPlayerTitle)
        fullTitle = findViewById(R.id.fullPlayerTitle)
        miniPlayBtn = findViewById(R.id.miniPlayerPlayPause)
        fullPlayBtn = findViewById(R.id.fullPlayerPlayPause)
        miniImg = findViewById(R.id.miniPlayerImg)
        fullImg = findViewById(R.id.fullPlayerImg)

        seekBar = findViewById(R.id.playerSeekBar)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalDuration = findViewById(R.id.tvTotalDuration)

        suggestionRv = findViewById(R.id.suggestionRecyclerView)
        suggestionRv.layoutManager = GridLayoutManager(this, 2)

        findViewById<ImageView>(R.id.btnSettings)?.setOnClickListener { showLanguageDialog() }

        findViewById<ImageButton>(R.id.btnClosePlayer)?.setOnClickListener { closePlayer() }

        val playPauseListener = View.OnClickListener {
            controller?.let { if (it.isPlaying) it.pause() else it.play() }
        }
        miniPlayBtn?.setOnClickListener(playPauseListener)
        fullPlayBtn?.setOnClickListener(playPauseListener)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) controller?.seekTo(p.toLong())
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
    }

    // 🕒 Seekbar Update Task
    private val updateSeekBarTask = object : Runnable {
        override fun run() {
            controller?.let {
                if (it.isPlaying) {
                    seekBar.max = it.duration.toInt()
                    seekBar.progress = it.currentPosition.toInt()
                    tvCurrentTime.text = formatTime(it.currentPosition)
                    tvTotalDuration.text = formatTime(it.duration)
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    private fun formatTime(ms: Long): String {
        if (ms < 0) return "00:00"
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }

    // 🔋 Smart Battery Check
    private fun getBatteryPercentage(): Int {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) (level * 100 / scale) else 100
    }

    fun playRadio(station: RadioStation) {
        if (controller == null) return
        currentIndex = -1
        startPlayback(station.name, station.stream_url, station.image_url, true)
    }

    fun playPodcast(playlist: List<Podcast>, index: Int) {
        if (controller == null || index < 0 || index >= playlist.size) return
        this.currentPlaylist = playlist
        this.currentIndex = index
        val p = playlist[index]
        startPlayback(p.title ?: "", p.audio_url ?: "", p.image_url ?: "", false)
        updateSuggestions()
    }

    private fun startPlayback(title: String, url: String, img: String, isRadio: Boolean) {
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(title).setArtworkUri(Uri.parse(img)).build())
            .build()

        controller?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        updateUI(title, img)
        showFullPlayer()

        if (!isRadio) {
            seekBar.visibility = View.VISIBLE
            handler.post(updateSeekBarTask)
        } else {
            seekBar.visibility = View.INVISIBLE
            tvCurrentTime.text = "LIVE"
            tvTotalDuration.text = ""
        }
    }

    private fun updateSuggestions() {
        val suggestions = currentPlaylist.filterIndexed { index, _ -> index != currentIndex }
        suggestionRv.adapter = PodcastAdapter(suggestions)
    }

    private fun updateUI(title: String, imageUrl: String) {
        miniTitle?.text = title
        fullTitle?.text = title
        miniImg?.let { Glide.with(this).load(imageUrl).into(it) }
        fullImg?.let { Glide.with(this).load(imageUrl).into(it) }
    }

    private fun setupFloatingAndGestures() {
        var dX = 0f
        var dY = 0f
        var isMoving = false

        miniBar.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { dX = view.x - event.rawX; dY = view.y - event.rawY; isMoving = false }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX; val newY = event.rawY + dY
                    if (abs(newX - view.x) > 15 || abs(newY - view.y) > 15) {
                        isMoving = true; view.x = newX; view.y = newY
                    }
                }
                MotionEvent.ACTION_UP -> { if (!isMoving) showFullPlayer() else snapToEdge(view) }
            }
            true
        }

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                if (vY > 400 && abs(vY) > abs(vX)) { showMiniPlayer(); return true }
                return false
            }
        })

        fullPlayerScrollView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun snapToEdge(view: View) {
        val screenWidth = resources.displayMetrics.widthPixels
        val margin = 32f
        val finalX = if (view.x + view.width / 2 < screenWidth / 2) margin else (screenWidth - view.width - margin)
        view.animate().x(finalX).setDuration(300).start()
    }

    private fun showFullPlayer() {
        fullPlayer.visibility = View.VISIBLE
        miniBar.visibility = View.GONE
        bottomNav.visibility = View.GONE
    }

    private fun showMiniPlayer() {
        fullPlayer.visibility = View.GONE
        miniBar.visibility = View.VISIBLE
        bottomNav.visibility = View.VISIBLE
    }

    private fun closePlayer() {
        miniBar.visibility = View.GONE
        fullPlayer.visibility = View.GONE
        controller?.stop()
        handler.removeCallbacks(updateSeekBarTask)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStart() {
        super.onStart()
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        future.addListener({
            controller = future.get()
            controller?.addListener(object : Player.Listener {

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    val battery = getBatteryPercentage()

                    // 🔥 Smart Ads Logic
                    if (isPlaying && battery > 15) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }

                    val icon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                    miniPlayBtn?.setImageResource(icon)
                    fullPlayBtn?.setImageResource(icon)

                    if (isPlaying) handler.post(updateSeekBarTask) else handler.removeCallbacks(updateSeekBarTask)
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        if (currentIndex < currentPlaylist.size - 1) playPodcast(currentPlaylist, currentIndex + 1)
                        else closePlayer()
                    }
                }

                override fun onMediaMetadataChanged(metadata: MediaMetadata) {
                    updateUI(metadata.title?.toString() ?: "", metadata.artworkUri.toString())
                }
            })
        }, MoreExecutors.directExecutor())
    }

    private fun setupNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_news -> { categoryScroll.visibility = View.VISIBLE; loadFragment(NewsFragment()); true }
                R.id.nav_podcast -> { categoryScroll.visibility = View.GONE; loadFragment(PodcastFragment()); true }
                R.id.nav_radio -> { categoryScroll.visibility = View.GONE; loadFragment(RadioFragment()); true }
                else -> false
            }
        }
    }

    private fun setupChipLogic() {
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val category = when (checkedIds[0]) {
                    R.id.chipGeneral -> "general"
                    R.id.chipBusiness -> "business"
                    R.id.chipSports -> "sports"
                    R.id.chipTech -> "tech"
                    else -> "general"
                }
                if (category != currentCategory) {
                    currentCategory = category
                    (supportFragmentManager.findFragmentById(R.id.fragment_container) as? NewsFragment)?.updateCategory(category)
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment) = supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment).commit()

    private fun checkFirstTimeUser() {
        val prefs = getSharedPreferences("YashoraPrefs", MODE_PRIVATE)
        if (!prefs.contains("selected_lang")) showLanguageDialog() else loadFragment(NewsFragment())
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("हिंदी", "English", "मराठी", "ਪੰਜਾਬੀ")
        val langCodes = arrayOf("hi", "en", "mr", "pa")
        AlertDialog.Builder(this).setTitle("Language").setItems(languages) { _, which ->
            LocaleHelper.setLanguage(this, langCodes[which])
            bottomNav.selectedItemId = R.id.nav_news
            loadFragment(NewsFragment())
        }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBarTask)
        controller?.release()
    }
}