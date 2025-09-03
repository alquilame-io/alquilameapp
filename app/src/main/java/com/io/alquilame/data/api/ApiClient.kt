package com.io.alquilame.data.api

import android.content.Context
import com.io.alquilame.data.SecureCredentialsStorage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(private val context: Context) {
    
    companion object {
        private const val BASE_URL = "https://alquilame.io/"
        private const val TIMEOUT_SECONDS = 30L
    }
    
    private val credentialsStorage = SecureCredentialsStorage(context)
    
    private val httpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val reservationsApiService: ReservationsApiService by lazy {
        retrofit.create(ReservationsApiService::class.java)
    }
    
    fun getAuthorizationHeader(): String? {
        val apiKey = credentialsStorage.getApiKey()
        return if (apiKey != null) "Bearer $apiKey" else null
    }
    
    fun hasCredentials(): Boolean {
        return credentialsStorage.hasCredentials()
    }
}