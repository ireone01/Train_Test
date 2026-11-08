package com.example.test.ui.Splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.test.R
import com.example.test.ui.theme.TestTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val FAKE_LOADING_DURATION_MS = 5_000L
private const val PROGRESS_UPDATE_INTERVAL_MS = 50L

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val steps = (FAKE_LOADING_DURATION_MS / PROGRESS_UPDATE_INTERVAL_MS).toInt()
        repeat(steps) { step ->

            delay(PROGRESS_UPDATE_INTERVAL_MS.milliseconds)
            progress = (step + 1) / steps.toFloat()
        }
        isLoading = false
        onFinished()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.app_name))

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = if (isLoading) stringResource(R.string.loading) else stringResource(R.string.done))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.width(200.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("${(progress * 100).toInt()}%")
        Spacer(modifier = Modifier.height(12.dp))
        if (isLoading) CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}