package com.example.test

import android.app.Application
import android.util.Log
import com.example.test.di.AppContainer
import com.example.test.data.remote.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class InventoryApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
        applicationScope.launch {
            runCatching { AppContainer.equipmentRepository.initialize() }
                .onFailure { Log.e(TAG, "Could not seed the initial equipment catalog", it) }
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
