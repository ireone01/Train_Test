package com.example.test

import android.app.Application
import android.util.Log
import com.example.test.di.AppContainer
import com.example.test.data.remote.NetworkModule
import com.example.test.platform.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class InventoryApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
        NotificationHelper.createChannel(this)
        applicationScope.launch {
            runCatching { AppContainer.equipmentRepository.initialize() }
                .onFailure { Log.e(TAG, "Could not synchronize the API snapshot into Room", it) }
        }
    }

    override fun onTerminate() {
        NetworkModule.close()
        super.onTerminate()
    }

    private companion object {
        const val TAG = "VNPTInventory"
    }
}
