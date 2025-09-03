package com.io.alquilame.data

import android.content.Context
import com.io.alquilame.data.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ username and API key credentials and retrieves user information.
 */
class LoginDataSource(private val context: Context) {
    
    private val credentialsStorage = SecureCredentialsStorage(context)

    fun login(username: String, apiKey: String): Result<LoggedInUser> {
        try {
            if (username.isBlank() || apiKey.isBlank()) {
                return Result.Error(IOException("Username and API key are required"))
            }
            
            // Store credentials securely
            credentialsStorage.saveCredentials(username, apiKey)
            
            // Create user object with the provided username
            val user = LoggedInUser(username, username)
            return Result.Success(user)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        credentialsStorage.clearCredentials()
    }
    
    fun getStoredUsername(): String? {
        return credentialsStorage.getUsername()
    }
    
    fun getStoredApiKey(): String? {
        return credentialsStorage.getApiKey()
    }
    
    fun hasStoredCredentials(): Boolean {
        return credentialsStorage.hasCredentials()
    }
}