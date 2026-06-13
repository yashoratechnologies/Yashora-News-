package com.surya.yashoranews.DataModels

// 1. 🔥 Ye poore JSON response ko handle karega
data class NewsMainResponse(
    val success: Boolean,
    val count: Int?,
    val lastDate: String?, // 👈 IMPORTANT: Agli request ke liye ye cursor zaroori hai
    val articles: List<Article>,
    val source: String?    // Optional: Ye batayega data "cache" se aaya ya "database" se
)

// 2. 📰 Ye ek single news item ko handle karega
data class Article(
    val id: String?,
    val title: String?,
    val summary: String?,    // Backend se summarize news aayegi
    val image_url: String?,  // Full URL of the image
    val link: String?,       // Original news source link
    val source: String?,

    val published_at: String? // 🔥 Cursor pagination ke liye ye date zaroori hai
)