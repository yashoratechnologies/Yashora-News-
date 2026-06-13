package com.surya.yashoranews.DataModels

data class RadioStation(
    val id: String,
    val name: String,
    val stream_url: String,
    val image_url: String,
    val language: String,
    val category: String,
    val is_active: Boolean
)