package com.example.hw2.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface SentimentAnalysisApi {
    @POST("/predict")
    suspend fun getSentiment(@Body request: PhoBERTRequest): retrofit2.Response<PhoBERTResponse>
}

class RetrofitInstance(baseUrl: String) {
    val api: SentimentAnalysisApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SentimentAnalysisApi::class.java)
    }
}