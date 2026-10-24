package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TwseAuctionResponse(
    @Json(name = "stat") val stat: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "fields") val fields: List<String>? = null,
    @Json(name = "data") val data: List<List<String>>? = null
)
