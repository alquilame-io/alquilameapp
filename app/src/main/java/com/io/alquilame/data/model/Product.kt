package com.io.alquilame.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("price")
    val price: String,
    
    @SerializedName("code")
    val code: String,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("location")
    val location: String,
    
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName("inserted_at")
    val insertedAt: String
)

data class Tenant(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("slug")
    val slug: String
)

data class ProductsResponse(
    @SerializedName("data")
    val data: List<Product>,
    
    @SerializedName("tenant")
    val tenant: Tenant,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("per_page")
    val perPage: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int,
    
    @SerializedName("total_count")
    val totalCount: Int
)

data class AvailabilityDay(
    @SerializedName("date")
    val date: String,
    
    @SerializedName("available")
    val available: Int,
    
    @SerializedName("reserved")
    val reserved: Int,
    
    @SerializedName("total")
    val total: Int
)

data class AvailabilityResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: List<AvailabilityDay>,
    
    @SerializedName("message")
    val message: String?
)

data class ReservationRequest(
    @SerializedName("reservation")
    val reservation: ReservationData
)

data class ReservationData(
    @SerializedName("merchandise_id")
    val merchandiseId: String,
    
    @SerializedName("client_name")
    val clientName: String,
    
    @SerializedName("client_phone")
    val clientPhone: String?,
    
    @SerializedName("client_email")
    val clientEmail: String?,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("quantity")
    val quantity: String,
    
    @SerializedName("notes")
    val notes: String?
)

data class ReservationCreateResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: CreatedReservationData?,
    
    @SerializedName("message")
    val message: String
)

data class CreatedReservationData(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("merchandise_id")
    val merchandiseId: Int,
    
    @SerializedName("merchandise_name")
    val merchandiseName: String,
    
    @SerializedName("client_name")
    val clientName: String,
    
    @SerializedName("client_phone")
    val clientPhone: String?,
    
    @SerializedName("client_email")
    val clientEmail: String?,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("tenant")
    val tenant: Tenant,
    
    @SerializedName("inserted_at")
    val insertedAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)