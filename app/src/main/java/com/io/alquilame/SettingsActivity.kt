package com.io.alquilame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.io.alquilame.data.SecureCredentialsStorage
import com.io.alquilame.ui.login.LoginActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var credentialsStorage: SecureCredentialsStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)

        credentialsStorage = SecureCredentialsStorage(this)

        setupClearLoginButton()
    }

    private fun setupClearLoginButton() {
        val btnClearLoginData: Button = findViewById(R.id.btn_clear_login_data)
        
        btnClearLoginData.setOnClickListener {
            showClearDataConfirmationDialog()
        }
    }

    private fun showClearDataConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_login_data))
            .setMessage(getString(R.string.clear_data_confirmation))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                clearLoginData()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun clearLoginData() {
        try {
            // Clear stored credentials
            credentialsStorage.clearCredentials()
            
            // Show success message
            Toast.makeText(this, getString(R.string.data_cleared), Toast.LENGTH_SHORT).show()
            
            // Redirect to login activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error clearing login data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}