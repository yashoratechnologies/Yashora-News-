package com.surya.yashoranews

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class RadioPlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var radioImage: ImageView
    private lateinit var radioTitle: TextView
    private lateinit var radioStatus: TextView
    private lateinit var loader: ProgressBar
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radio_player)

        initializeViews()
        handleIntentData()
    }

    private fun initializeViews() {
        radioImage = findViewById(R.id.radioPlayerImage)
        radioTitle = findViewById(R.id.radioPlayerTitle)
        radioStatus = findViewById(R.id.radioStatus)
        loader = findViewById(R.id.radioPlayerLoader)
        btnBack = findViewById(R.id.btnBackRadio)

        btnBack.setOnClickListener { finish() }
    }

    private fun handleIntentData() {
        val name = intent.getStringExtra("name") ?: "FM Radio"
        val url = intent.getStringExtra("streamUrl") ?: ""
        val image = intent.getStringExtra("imageUrl") ?: ""

        radioTitle.text = name

        Glide.with(this)
            .load(image)
            .placeholder(R.drawable.placeholder_radio)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(radioImage)

        if (url.isNotEmpty()) {
            setupPlayer(url)
        } else {
            Toast.makeText(this, "Invalid Stream URL", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer(url: String) {
        player?.release()

        player = ExoPlayer.Builder(this).build()
        val playerControlView = findViewById<PlayerControlView>(R.id.radioControlView)
        playerControlView.player = player

        // 🔥 HLS (.m3u8) vs MP3 logic
        val mediaItem = if (url.contains(".m3u8") || url.contains("playlist")) {
            MediaItem.Builder()
                .setUri(url)
                .setMimeType(MimeTypes.APPLICATION_M3U8) // HLS support fix
                .build()
        } else {
            MediaItem.fromUri(url)
        }

        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        loader.visibility = View.VISIBLE
                        radioStatus.text = "Buffering..."
                    }
                    Player.STATE_READY -> {
                        loader.visibility = View.GONE
                        radioStatus.text = "• LIVE"
                    }
                    Player.STATE_ENDED -> radioStatus.text = "Stream Ended"
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                loader.visibility = View.GONE
                radioStatus.text = "Stream Offline"
                Toast.makeText(this@RadioPlayerActivity, "Link is down, try another station", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        // Radio hamesha background mein nahi chalna chahiye jab tak Service na ho
        // Isliye stop kar dete hain memory bachane ke liye
        player?.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}