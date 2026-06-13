package com.surya.yashoranews

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        // 1. New Feature: Player with Audio Focus & Noisy Handling
        val player = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true // यह पक्का करता है कि कॉल आने पर रेडियो अपने आप धीमा/बंद हो जाए
            )
            setHandleAudioBecomingNoisy(true) // हेडफ़ोन निकलने पर गाना अपने आप रुक जाएगा
        }

        // 2. MediaSession तैयार करो
        mediaSession = MediaSession.Builder(this, player).build()
    }

    // बैकग्राउंड कनेक्शन के लिए
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    // 🔥 Old Feature: ऐप स्वाइप करने पर सर्विस कंट्रोल करना (वापस जोड़ दिया गया है)
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player != null) {
            // अगर प्लेयर रुका हुआ है या कोई गाना नहीं है, तो सर्विस बंद कर दो
            if (!player.playWhenReady || player.mediaItemCount == 0) {
                stopSelf()
            }
        }
    }

    // मेमोरी साफ़ करने के लिए
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}