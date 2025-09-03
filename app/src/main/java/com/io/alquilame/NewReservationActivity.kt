package com.io.alquilame

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.io.alquilame.data.api.ApiClient
import com.io.alquilame.data.model.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NewReservationActivity : AppCompatActivity() {

    private lateinit var apiClient: ApiClient
    private var products: List<Product> = emptyList()
    private var selectedProduct: Product? = null
    private var selectedDate: String? = null

    // Views
    private lateinit var spinnerProducts: Spinner
    private lateinit var tvAvailabilityInfo: TextView
    private lateinit var etClientName: TextInputEditText
    private lateinit var etClientPhone: TextInputEditText
    private lateinit var etClientEmail: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etQuantity: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private lateinit var btnCreateReservation: Button
    private lateinit var btnCancel: Button
    private lateinit var loadingOverlay: FrameLayout
    
    // Input layouts for error handling
    private lateinit var tilClientName: TextInputLayout
    private lateinit var tilDate: TextInputLayout
    private lateinit var tilQuantity: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_reservation)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "New Reservation"

        initViews()
        apiClient = ApiClient(this)
        
        setupListeners()
        loadProducts()
    }

    private fun initViews() {
        spinnerProducts = findViewById(R.id.spinner_products)
        tvAvailabilityInfo = findViewById(R.id.tv_availability_info)
        etClientName = findViewById(R.id.et_client_name)
        etClientPhone = findViewById(R.id.et_client_phone)
        etClientEmail = findViewById(R.id.et_client_email)
        etDate = findViewById(R.id.et_date)
        etQuantity = findViewById(R.id.et_quantity)
        etNotes = findViewById(R.id.et_notes)
        btnCreateReservation = findViewById(R.id.btn_create_reservation)
        btnCancel = findViewById(R.id.btn_cancel)
        loadingOverlay = findViewById(R.id.loading_overlay)
        
        tilClientName = findViewById(R.id.til_client_name)
        tilDate = findViewById(R.id.til_date)
        tilQuantity = findViewById(R.id.til_quantity)
    }

    private fun setupListeners() {
        btnCancel.setOnClickListener {
            finish()
        }

        btnCreateReservation.setOnClickListener {
            createReservation()
        }

        etDate.setOnClickListener {
            showDatePicker()
        }

        spinnerProducts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && products.isNotEmpty()) {
                    selectedProduct = products[position - 1]
                    checkAvailability()
                } else {
                    selectedProduct = null
                    tvAvailabilityInfo.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedProduct = null
                tvAvailabilityInfo.visibility = View.GONE
            }
        }
    }

    private fun loadProducts() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val authHeader = apiClient.getAuthorizationHeader()
                if (authHeader == null) {
                    showError("No authentication credentials found")
                    return@launch
                }

                val response = apiClient.reservationsApiService.getProducts(authHeader)
                if (response.isSuccessful) {
                    val productsResponse = response.body()
                    if (productsResponse != null) {
                        products = productsResponse.data
                        setupProductsSpinner()
                    } else {
                        showError("Failed to load products")
                    }
                } else {
                    showError("Failed to load products: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun setupProductsSpinner() {
        val productNames = mutableListOf("Select a product...")
        productNames.addAll(products.map { "${it.name} - ${it.category}" })
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, productNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProducts.adapter = adapter
    }

    private fun checkAvailability() {
        val product = selectedProduct ?: return
        val date = selectedDate ?: return

        lifecycleScope.launch {
            try {
                val authHeader = apiClient.getAuthorizationHeader()
                if (authHeader == null) return@launch

                val response = apiClient.reservationsApiService.checkAvailability(
                    authHeader, 
                    product.id,
                    startDate = date,
                    endDate = date
                )
                
                if (response.isSuccessful) {
                    val availabilityResponse = response.body()
                    if (availabilityResponse?.success == true && availabilityResponse.data.isNotEmpty()) {
                        val availability = availabilityResponse.data[0]
                        tvAvailabilityInfo.text = "Available: ${availability.available} units (Total: ${availability.total})"
                        tvAvailabilityInfo.visibility = View.VISIBLE
                    } else {
                        tvAvailabilityInfo.text = "No availability data"
                        tvAvailabilityInfo.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                // Silently handle availability check errors
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = sdf.format(calendar.time)
                etDate.setText(selectedDate)
                checkAvailability()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun createReservation() {
        if (!validateForm()) {
            return
        }

        val product = selectedProduct!!
        val clientName = etClientName.text.toString().trim()
        val clientPhone = etClientPhone.text.toString().trim().takeIf { it.isNotEmpty() }
        val clientEmail = etClientEmail.text.toString().trim().takeIf { it.isNotEmpty() }
        val date = selectedDate!!
        val quantity = etQuantity.text.toString().trim()
        val notes = etNotes.text.toString().trim().takeIf { it.isNotEmpty() }

        val reservationData = ReservationData(
            merchandiseId = product.id.toString(),
            clientName = clientName,
            clientPhone = clientPhone,
            clientEmail = clientEmail,
            date = date,
            quantity = quantity,
            notes = notes
        )

        val request = ReservationRequest(reservationData)

        showLoading(true)
        lifecycleScope.launch {
            try {
                val authHeader = apiClient.getAuthorizationHeader()
                if (authHeader == null) {
                    showError("No authentication credentials found")
                    return@launch
                }

                val response = apiClient.reservationsApiService.createReservation(authHeader, request = request)
                
                if (response.isSuccessful) {
                    val reservationResponse = response.body()
                    if (reservationResponse?.success == true) {
                        Toast.makeText(this@NewReservationActivity, reservationResponse.message, Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        showError(reservationResponse?.message ?: "Failed to create reservation")
                    }
                } else {
                    showError("Failed to create reservation: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Clear previous errors
        tilClientName.error = null
        tilDate.error = null
        tilQuantity.error = null

        // Validate client name
        if (etClientName.text.toString().trim().isEmpty()) {
            tilClientName.error = "Client name is required"
            isValid = false
        }

        // Validate product selection
        if (selectedProduct == null) {
            Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validate date
        if (selectedDate == null) {
            tilDate.error = "Date is required"
            isValid = false
        }

        // Validate quantity
        val quantityText = etQuantity.text.toString().trim()
        if (quantityText.isEmpty()) {
            tilQuantity.error = "Quantity is required"
            isValid = false
        } else {
            try {
                val quantity = quantityText.toInt()
                if (quantity <= 0) {
                    tilQuantity.error = "Quantity must be greater than 0"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                tilQuantity.error = "Invalid quantity"
                isValid = false
            }
        }

        // Validate at least one contact method
        val phone = etClientPhone.text.toString().trim()
        val email = etClientEmail.text.toString().trim()
        if (phone.isEmpty() && email.isEmpty()) {
            Toast.makeText(this, "Please provide either phone or email", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        btnCreateReservation.isEnabled = !show
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}