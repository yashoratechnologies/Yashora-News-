package com.surya.yashoranews.Adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.surya.yashoranews.DataModels.Podcast
import com.surya.yashoranews.MainActivity
import com.surya.yashoranews.R
import java.text.SimpleDateFormat
import java.util.*

class PodcastAdapter(
    private val list: List<Podcast>,
    private val isSuggestion: Boolean = false // Suggestions के लिए flag
) : RecyclerView.Adapter<PodcastAdapter.PodcastVH>() {

    class PodcastVH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.podcastImage)
        val title: TextView = v.findViewById(R.id.podcastTitle)
        val meta: TextView = v.findViewById(R.id.podcastMeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_podcast, parent, false)
        return PodcastVH(v)
    }

    override fun onBindViewHolder(holder: PodcastVH, position: Int) {
        val p = list[position]

        // 1. Title Set
        holder.title.text = p.title ?: "Yashora Podcast"

        // 2. Metadata तैयार करें (Source + Fixed Time Ago + Duration)
        val sourceName = p.source ?: "Yashora"
        val timeAgo = formatTimeAgo(p.published_at) // 🔥 अब यह सही टाइम बताएगा
        val durationText = formatDuration(p.duration_sec)

        holder.meta.text = "$sourceName • $timeAgo • $durationText"

        // 3. Glide Image Loading (Disk Cache Optimized)
        Glide.with(holder.itemView.context)
            .load(p.image_url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder_poster)
            .centerCrop()
            .into(holder.img)

        // 4. 🔥 Playlist & Index Logic (For Auto-play & Suggestions)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            if (context is MainActivity) {
                // पूरी लिस्ट और पोजीशन भेज रहे हैं
                context.playPodcast(list, position)
            } else {
                Toast.makeText(context, "Player Error: Try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = list.size

    // 🕒 Seconds ko "MM:SS" format mein badalne ke liye
    private fun formatDuration(seconds: Int): String {
        if (seconds <= 0) return "10:00"
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    // 📅 🔥 FIXED Time Ago Function (Multi-Format Support)
    private fun formatTimeAgo(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "Latest"

        return try {
            // बैकएंड से आने वाले संभावित फॉर्मेट्स
            val formats = arrayOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "EEE, dd MMM yyyy HH:mm:ss Z" // RSS फीड्स के लिए
            )

            var date: Date? = null
            for (f in formats) {
                try {
                    val sdf = SimpleDateFormat(f, Locale.getDefault())
                    // अगर फॉर्मेट में 'Z' है, तो उसे UTC मानकर चलें
                    if (f.contains("'Z'") || f.contains("Z")) {
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                    }
                    date = sdf.parse(dateStr)
                    if (date != null) break
                } catch (e: Exception) { continue }
            }

            if (date != null) {
                val timeInMillis = date.time
                val now = System.currentTimeMillis()

                // अगर तारीख भविष्य की लग रही है (Timezone mismatch), तो उसे "Just now" दिखाएँ
                if (timeInMillis > now) return "Just now"

                // Relative time: "2 hours ago", "Yesterday", etc.
                DateUtils.getRelativeTimeSpanString(
                    timeInMillis,
                    now,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString()
            } else {
                "Latest"
            }
        } catch (e: Exception) {
            "Latest"
        }
    }
}