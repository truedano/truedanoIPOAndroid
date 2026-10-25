package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TwseSubscriptionResponse(
    @Json(name = "stat") val stat: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "fields") val fields: List<String>? = null,
    @Json(name = "data") val data: List<List<String>>? = null
)

@JsonClass(generateAdapter = true)
data class TwseOpenApiStockQuote(
    @Json(name = "Code") val code: String? = null,
    @Json(name = "ClosingPrice") val closingPrice: String? = null
)

@JsonClass(generateAdapter = true)
data class TpexOpenApiStockQuote(
    @Json(name = "SecuritiesCompanyCode") val code: String? = null,
    @Json(name = "Close") val close: String? = null
)
