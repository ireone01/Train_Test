package com.example.test.ui.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.ui.Login.repository.AuthLogin
import com.example.test.ui.Login.repository.AuthLoginImpl
import com.example.test.ui.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginScreenViewModel(val authLogin: AuthLogin = AuthLoginImpl()) : ViewModel() {
    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _loginResult = MutableStateFlow<UserRole?>(null)
    val loginResult = _loginResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun onUsernameChanges(userName: String) {
        _username.value = userName
    }

    fun onPasswordChanges(password: String) {
        _password.value = password
    }

    fun login() {
        viewModelScope.launch(Dispatchers.Main) {
            val role = withContext(Dispatchers.IO) {
                authLogin.login(
                    phone = username.value,
                    password = password.value
                )
            }

            _loginResult.value = role
            _errorMessage.value = if (role == null) "Wrong SDT or Password" else ""

        }
    }
}