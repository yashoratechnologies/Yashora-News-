package com.surya.yashoranews.Retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {

    // 🔥 यहाँ अपना Render वाला URL डालो
    // ध्यान रहे: अंत में '/' होना ज़रूरी है
    private const val BASE_URL = "https://yashora-news-backend.onrender.com/api/"


    val api: NewsInterface by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsInterface::class.java)
    }
}