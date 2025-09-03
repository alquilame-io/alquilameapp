package com.io.alquilame.data.model

import com.google.gson.annotations.SerializedName

data class Reservation(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("client_name")
    val clientName: String,
    
    @SerializedName("client_phone")
    val clientPhone: String,
    
    @SerializedName("client_email")
    val clientEmail: String,
    
    @SerializedName("merchandise")
    val merchandise: Merchandise,
    
    @SerializedName("inserted_at")
    val insertedAt: String
)

data class Merchandise(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("image_url")
    val imageUrl: String
)

data class Pagination(
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("per_page")
    val perPage: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int,
    
    @SerializedName("total_count")
    val totalCount: Int
)

data class ReservationsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: List<Reservation>,
    
    @SerializedName("pagination")
    val pagination: Pagination,
    
    @SerializedName("message")
    val message: String
)