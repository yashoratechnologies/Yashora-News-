package com.surya.yashoranews.DataModels

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 🔥 Pro Level Model: Backend (TypeScript) ke naye features
 * (Duration in seconds, Language Boost, Source Links) ke liye fix kiya gaya hai.
 */
data class Podcast(
    @SerializedName("title")
    val title: String,

    @SerializedName("audio_url")
    val audio_url: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("image_url")
    val image_url: String,

    @SerializedName("duration_sec") // 🔥 Backend ab seconds bhej raha hai
    val duration_sec: Int,

    @SerializedName("published_at")
    val published_at: String,

    @SerializedName("source")
    val source: String?,

    @SerializedName("source_link") // 🔥 Naya Field
    val source_link: String?,

    @SerializedName("category")
    val category: String,

    @SerializedName("language") // 🔥 Lang selection ke liye
    val language: String
) : Serializable

/**
 * Netflix Style Home API ke liye Response Model
 * Kyunki ab API ek list nahi balki Categories ka object bhej rahi hai
 */
data class HomeResponse(
    val news: List<Podcast>?,
    val stories: List<Podcast>?,
    val mythology: List<Podcast>?,
    val crime: List<Podcast>?,
    val kids: List<Podcast>?,
    val motivation: List<Podcast>?,
    val spirituality: List<Podcast>?,
    val aajtak: List<Podcast>?,
    val audiobook: List<Podcast>?,
    val knowledge: List<Podcast>?
)

// Single category fetch karne ke liye (Simple list response)
data class PodcastResponse(
    val success: Boolean,
    val podcasts: List<Podcast>
)