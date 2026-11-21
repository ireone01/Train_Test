package com.example.test.ui.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.ui.Login.repository.AuthLogin
import com.example.test.ui.Login.repository.AuthLoginImpl
import com.example.test.ui.model.UserRole
import com.example.test.ui.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel(val authLogin: AuthLogin = AuthLoginImpl()) : ViewModel() {
    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _loginResult = MutableStateFlow<UserSession?>(null)
    val loginResult = _loginResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun onUsernameChanges(userName: String) {
        _username.value = userName
        _errorMessage.value = null
    }

    fun onPasswordChanges(password: String) {
        _password.value = password
        _errorMessage.value = null
    }

    fun login() {
        viewModelScope.launch {
            _loginResult.value = null
            _errorMessage.value = null
            val phone = username.value
            val role = authLogin.login(phone = phone, password = password.value)
            if (role == null) {
                _errorMessage.value = "Số điện thoại hoặc mật khẩu không đúng"
            } else {
                _loginResult.value = UserSession(phone = phone, role = role)
            }
        }
    }
}
