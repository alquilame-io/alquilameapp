package com.io.alquilame.data

import com.io.alquilame.data.model.LoggedInUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null || dataSource.hasStoredCredentials()

    init {
        // Load stored credentials if they exist
        if (dataSource.hasStoredCredentials()) {
            val username = dataSource.getStoredUsername()
            if (username != null) {
                user = LoggedInUser(username, username)
            }
        }
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    fun login(username: String, apiKey: String): Result<LoggedInUser> {
        // handle login with API key
        val result = dataSource.login(username, apiKey)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
    }
    
    fun getStoredApiKey(): String? {
        return dataSource.getStoredApiKey()
    }
}