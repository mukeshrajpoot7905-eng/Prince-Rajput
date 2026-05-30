package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ExchangeRateResponse(
    @Json(name = "result") val result: String,
    @Json(name = "base_code") val baseCode: String,
    @Json(name = "rates") val rates: Map<String, Double>,
    @Json(name = "time_last_update_utc") val timeLastUpdateUtc: String? = null
)

interface ExchangeRateService {
    @GET("v6/latest/{base}")
    suspend fun getLatestRates(@Path("base") base: String = "USD"): ExchangeRateResponse
}

object RetrofitClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://open.er-api.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: ExchangeRateService = retrofit.create(ExchangeRateService::class.java)
}
