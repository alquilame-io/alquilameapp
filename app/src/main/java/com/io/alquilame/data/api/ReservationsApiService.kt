package com.io.alquilame.data.api

import com.io.alquilame.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ReservationsApiService {
    
    @GET("api/mobile/reservations")
    suspend fun getReservations(
        @Header("Authorization") authorization: String,
        @Query("from_date") fromDate: String? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ReservationsResponse>
    
    @GET("api/mobile/products")
    suspend fun getProducts(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("category") category: String? = null
    ): Response<ProductsResponse>
    
    @GET("api/mobile/availability")
    suspend fun checkAvailability(
        @Header("Authorization") authorization: String,
        @Query("product_id") productId: Int,
        @Query("days") days: Int? = null,
        @Query("month") month: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<AvailabilityResponse>
    
    @POST("api/mobile/reservations")
    suspend fun createReservation(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ReservationRequest
    ): Response<ReservationCreateResponse>
}