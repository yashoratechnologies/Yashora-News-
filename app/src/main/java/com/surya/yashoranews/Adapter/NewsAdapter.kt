package com.surya.yashoranews.Adapter

import android.content.Intent
import android.annotation.SuppressLint
import android.net.Uri
import android.text.Html
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.surya.yashoranews.DataModels.Article
import com.surya.yashoranews.R
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(private val newsList: List<Article>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.newsImage)
        val title: TextView = itemView.findViewById(R.id.newsTitle)
        val summary: TextView = itemView.findViewById(R.id.newsSummary)
        val source: TextView = itemView.findViewById(R.id.newsSource)
        val time: TextView = itemView.findViewById(R.id.newsTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = newsList[position]

        // Data binding
        holder.title.text = Html.fromHtml(article.title ?: "", Html.FROM_HTML_MODE_LEGACY)
        holder.summary.text = Html.fromHtml(article.summary ?: "", Html.FROM_HTML_MODE_LEGACY)
        holder.source.text = article.source ?: "Yashora News"
        holder.time.text = getTimeAgo(article.published_at)

        Glide.with(holder.itemView.context)
            .load(article.image_url)
            .placeholder(R.drawable.placeholder_image)
            .centerCrop()
            .into(holder.img)

        // 🔥 DOUBLE CLICK LOGIC
        val gestureDetector = GestureDetector(holder.itemView.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val link = article.link
                if (!link.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    holder.itemView.context.startActivity(intent)
                }
                return true
            }

            // Single tap par news expand ya normal click handle kar sakte ho agar chahiye toh
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                return true
            }
        })

        holder.itemView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    override fun getItemCount(): Int = newsList.size

    private fun getTimeAgo(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "अभी-अभी"
        val formats = arrayOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd HH:mm:ss")
        var date: Date? = null
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                date = sdf.parse(dateString)
                if (date != null) break
            } catch (e: Exception) { continue }
        }
        if (date == null) return "हाल ही में"
        val diff = System.currentTimeMillis() - date.time
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        return when {
            minutes < 1 -> "अभी-अभी"
            minutes < 60 -> "$minutes मिनट पहले"
            hours < 24 -> "$hours घंटे पहले"
            else -> "${hours / 24} दिन पहले"
        }
    }
}