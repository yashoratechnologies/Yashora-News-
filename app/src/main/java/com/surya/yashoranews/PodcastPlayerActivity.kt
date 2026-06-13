package com.surya.yashoranews

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.surya.yashoranews.Adapter.PodcastAdapter
import com.surya.yashoranews.DataModels.Podcast
import java.util.Locale

class PodcastPlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var playerSource: TextView
    private lateinit var playerTitle: TextView
    private lateinit var playerImg: ImageView
    private lateinit var loader: ProgressBar
    private lateinit var rvSuggestions: RecyclerView

    private var currentIndex: Int = 0
    private var fullList: ArrayList<Podcast> = arrayListOf()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast_player)

        initializeViews()
        handleIntentData(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentData(intent)
    }

    private fun initializeViews() {
        playerImg = findViewById(R.id.playerImage)
        playerTitle = findViewById(R.id.playerTitle)
        playerSource = findViewById(R.id.playerSource)
        seekBar = findViewById(R.id.playerSeekBar)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalDuration = findViewById(R.id.tvTotalTime)
        loader = findViewById(R.id.podcastLoader)
        rvSuggestions = findViewById(R.id.rvSuggestions)

        rvSuggestions.layoutManager = GridLayoutManager(this, 2)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) player?.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun handleIntentData(intent: Intent?) {
        intent?.let {
            currentIndex = it.getIntExtra("currentIndex", 0)
            val listFromManager = PodcastDataManager.currentList

            if (listFromManager.isNotEmpty()) {
                fullList = ArrayList(listFromManager)
                val currentPodcast = fullList[currentIndex]
                val audioUrl = it.getStringExtra("audioUrl") ?: currentPodcast.audio_url

                updateUI(currentPodcast)

                if (!audioUrl.isNullOrEmpty()) {
                    setupPlayer(audioUrl)
                }

                rvSuggestions.adapter = PodcastAdapter(fullList)
            }
        }
    }

    private fun updateUI(podcast: Podcast) {
        playerTitle.text = podcast.title
        playerSource.text = podcast.source ?: "Yashora News"

        // 🔥 UI Reset aur Naya Seconds format use karna
        seekBar.progress = 0
        tvCurrentTime.text = "00:00"

        // Agar backend se seconds aa rahe hain to use convert karein
        tvTotalDuration.text = formatSecondsToTime(podcast.duration_sec)

        Glide.with(this)
            .load(podcast.image_url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder_poster)
            .error(R.drawable.placeholder_poster)
            .centerCrop()
            .into(playerImg)
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer(url: String) {
        player?.stop()
        player?.release()

        // Fast load control
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(2500, 10000, 1000, 1500)
            .build()

        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .setSeekParameters(SeekParameters.CLOSEST_SYNC)
            .build()

        findViewById<androidx.media3.ui.PlayerControlView>(R.id.playerControlView).player = player

        val mediaItem = MediaItem.fromUri(url)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> loader.visibility = View.VISIBLE
                    Player.STATE_READY -> {
                        loader.visibility = View.GONE
                        val duration = player?.duration ?: 0L
                        if (duration > 0) {
                            seekBar.max = duration.toInt()
                            // Real-time duration agar server wale se alag ho to update karein
                            tvTotalDuration.text = formatTime(duration)
                        }
                        updateProgress()
                    }
                    Player.STATE_ENDED -> playNextPodcast()
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                loader.visibility = View.GONE
                Toast.makeText(this@PodcastPlayerActivity, "Audio Link Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 🔥 Seconds ko 00:00 format mein badalne wala helper
    private fun formatSecondsToTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun playNextPodcast() {
        if (currentIndex < fullList.size - 1) {
            currentIndex++
            val next = fullList[currentIndex]
            updateUI(next)
            setupPlayer(next.audio_url)
        }
    }

    private fun updateProgress() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(object : Runnable {
            override fun run() {
                player?.let {
                    if (it.isPlaying) {
                        seekBar.progress = it.currentPosition.toInt()
                        tvCurrentTime.text = formatTime(it.currentPosition)
                    }
                    handler.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    private fun formatTime(ms: Long): String {
        val s = ms / 1000
        return String.format(Locale.getDefault(), "%02d:%02d", s / 60, s % 60)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        player?.release()
        player = null
    }
}