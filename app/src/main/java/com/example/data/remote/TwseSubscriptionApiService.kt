package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface TwseSubscriptionApiService {

    /**
     * 證交所公開申購抽籤日程表 API (對應 https://www.twse.com.tw/zh/announcement/public.html)
     */
    @GET("rwd/zh/announcement/publicForm")
    suspend fun getPublicLotteries(
        @Query("response") responseType: String = "json"
    ): TwseSubscriptionResponse

    /**
     * 上市普通股每日收盤價 (TWSE OpenAPI)
     */
    @GET("https://openapi.twse.com.tw/v1/exchangeReport/STOCK_DAY_ALL")
    suspend fun getTwseStockDayAll(): List<TwseOpenApiStockQuote>

    /**
     * 上櫃普通股每日收盤價 (TPEx OpenAPI)
     */
    @GET("https://www.tpex.org.tw/openapi/v1/tpex_mainboard_quotes")
    suspend fun getTpexStockQuotes(): List<TpexOpenApiStockQuote>

    companion object {
        private const val BASE_URL = "https://www.twse.com.tw/"

        fun create(): TwseSubscriptionApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .header("Accept", "application/json, text/javascript, */*; q=0.01")
                        .build()
                    chain.proceed(request)
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(TwseSubscriptionApiService::class.java)
        }
    }
}
