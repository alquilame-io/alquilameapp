package com.io.alquilame.data

import android.content.Context

/**
 * API client class that handles HTTP requests with stored credentials
 */
class ApiClient(private val context: Context) {
    
    private val credentialsStorage = SecureCredentialsStorage(context)
    
    /**
     * Get the stored API key for authenticated requests
     * @return The stored API key or null if not available
     */
    fun getApiKey(): String? {
        return credentialsStorage.getApiKey()
    }
    
    /**
     * Get the stored username
     * @return The stored username or null if not available
     */
    fun getUsername(): String? {
        return credentialsStorage.getUsername()
    }
    
    /**
     * Check if credentials are available for API requests
     * @return true if both username and API key are stored
     */
    fun hasCredentials(): Boolean {
        return credentialsStorage.hasCredentials()
    }
    
    /**
     * Get authentication headers for API requests
     * @return Map of headers including the API key authentication
     */
    fun getAuthHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        val apiKey = getApiKey()
        
        if (apiKey != null) {
            // Common API key header formats - adjust based on your API requirements
            headers["Authorization"] = "Bearer $apiKey"
            // Alternative formats:
            // headers["Authorization"] = "ApiKey $apiKey"
            // headers["X-API-Key"] = apiKey
        }
        
        return headers
    }
    
    /**
     * Example method for making authenticated API requests
     * You can extend this based on your specific API requirements
     */
    fun isAuthenticatedRequestPossible(): Boolean {
        return hasCredentials()
    }
}