package com.io.alquilame.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureCredentialsStorage(private val context: Context) {
    
    companion object {
        private const val PREFS_FILE_NAME = "secure_credentials"
        private const val KEY_USERNAME = "username"
        private const val KEY_API_KEY = "api_key"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedSharedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveCredentials(username: String, apiKey: String) {
        encryptedSharedPrefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_API_KEY, apiKey)
            .apply()
    }
    
    fun getUsername(): String? {
        return encryptedSharedPrefs.getString(KEY_USERNAME, null)
    }
    
    fun getApiKey(): String? {
        return encryptedSharedPrefs.getString(KEY_API_KEY, null)
    }
    
    fun hasCredentials(): Boolean {
        return getUsername() != null && getApiKey() != null
    }
    
    fun clearCredentials() {
        encryptedSharedPrefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_API_KEY)
            .apply()
    }
}