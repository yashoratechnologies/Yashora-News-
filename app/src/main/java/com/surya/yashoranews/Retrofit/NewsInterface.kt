package com.surya.yashoranews.Retrofit

import com.surya.yashoranews.DataModels.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsInterface {

    // ==========================================================
    // 📰 NEWS SECTION (Pagination & Category)
    // ==========================================================

    @GET("news") // 🔥 Fix: "api/" hata diya gaya hai kyunki ye BASE_URL mein hai
    fun getNews(
        @Query("lang") lang: String,
        @Query("category") category: String,
        @Query("limit") limit: Int,
        @Query("lastDate") lastDate: String?
    ): Call<NewsMainResponse>

    @GET("article")
    fun getArticle(
        @Query("url") url: String
    ): Call<ArticleResponse>


    // ==========================================================
    // 🎧 PODCAST SECTION (Pro Level - Netflix Style)
    // ==========================================================

    /**
     * 🔥 Home API (Netflix Style)
     * Ab ye sahi path "api/podcasts/home" par hit karega
     */
    @GET("podcasts/home")
    suspend fun getHomePodcasts(
        @Query("userLang") lang: String // "hi" ya "en"
    ): HomeResponse

    /**
     * Single category fetch: "api/podcasts/category/mythology"
     */
    @GET("podcasts/category/{cat}")
    suspend fun getCategoryPodcasts(
        @Path("cat") category: String,
        @Query("userLang") lang: String
    ): List<Podcast>

    /**
     * Legacy support
     */
    @GET("podcasts")
    fun getPodcasts(
        @Query("category") category: String,
        @Query("userLang") lang: String
    ): Call<PodcastResponse>


    @GET("radio")
    suspend fun getRadioStations(): List<RadioStation>
}