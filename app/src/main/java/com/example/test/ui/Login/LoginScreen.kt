package com.example.test.ui.Login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test.R
import com.example.test.ui.model.UserSession

@Composable
fun LoginScreen(onLoginSuccess: (UserSession) -> Unit, loginViewModel: LoginScreenViewModel) {

    val userName by loginViewModel.username.collectAsState()

    val password by loginViewModel.password.collectAsState()

    val errorMessage by loginViewModel.errorMessage.collectAsState()

    val loginResult by loginViewModel.loginResult.collectAsState()

    LaunchedEffect(loginResult) {
        loginResult?.let { session ->
            onLoginSuccess(session)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(250.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Text(text = stringResource(R.string.vnpt_test))

            Text(text = stringResource(R.string.system))

            OutlinedTextField(
                value = userName, onValueChange = {
                    loginViewModel.onUsernameChanges(it.filter { c -> c.isDigit() }.take(11))
                },
                label = { Text(stringResource(R.string.phone_number)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    loginViewModel.onPasswordChanges(it)
                },
                label = { Text(stringResource(R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

            Text(text = errorMessage ?: "")

            Button(
                modifier = Modifier.size(height = 46.dp, width = 68.dp),
                onClick = { loginViewModel.login() }
            ) {
                Text(text = stringResource(R.string.login))
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = {}, viewModel())
}
