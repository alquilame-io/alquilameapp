package com.io.alquilame.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.io.alquilame.data.LoginRepository
import com.io.alquilame.data.Result

import com.io.alquilame.R

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, apiKey: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, apiKey)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, apiKey: String) {
        if (!isUsernameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isApiKeyValid(apiKey)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isUsernameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    // API key validation - should be at least 8 characters
    private fun isApiKeyValid(apiKey: String): Boolean {
        return apiKey.length >= 8
    }
}