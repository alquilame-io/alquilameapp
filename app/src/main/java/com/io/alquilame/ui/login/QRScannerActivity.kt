package com.io.alquilame.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.io.alquilame.R
import com.io.alquilame.BuildConfig
import java.security.MessageDigest

class QRScannerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QR_RESULT = "qr_result"
        const val EXTRA_USERNAME = "username"
        const val EXTRA_API_KEY = "api_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start QR scanner immediately
        startQRScanner()
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan QR code to login")
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(false)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        
        if (result != null) {
            if (result.contents == null) {
                // User cancelled
                setResult(Activity.RESULT_CANCELED)
                finish()
            } else {
                // Process QR code result
                processQRResult(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun processQRResult(qrContent: String) {
        try {
            // QR content should be in format: "tenant_slug|tenant_secret_slug|hash"
            val parts = qrContent.split("|")
            
            if (parts.size == 3) {
                val tenantSlug = parts[0]
                val tenantSecretSlug = parts[1]
                val providedHash = parts[2]
                
                // Validate hash
                val expectedHash = generateHash(tenantSlug, tenantSecretSlug)
                
                if (providedHash == expectedHash) {
                    // Hash is valid, return credentials
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_USERNAME, tenantSlug)
                        putExtra(EXTRA_API_KEY, tenantSecretSlug)
                        putExtra(EXTRA_QR_RESULT, qrContent)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    showError("Invalid QR code. Hash validation failed.")
                }
            } else {
                showError("Invalid QR code format. Please scan a valid Alquilame QR code.")
            }
        } catch (e: Exception) {
            showError("Error processing QR code: ${e.message}")
        }
    }

    private fun generateHash(tenantSlug: String, tenantSecretSlug: String): String {
        val input = "$tenantSlug|$tenantSecretSlug|${BuildConfig.SECRET_STRING_LOGIN}"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}