package com.example.test.di

import android.content.Context
import com.example.test.data.local.InventoryDatabase
import com.example.test.data.remote.NetworkModule
import com.example.test.data.repository.RoomEquipmentRepository
import com.example.test.domain.repository.EquipmentRepository

/** Manual dependency container used in place of Hilt for this small app. */
object AppContainer {
    @Volatile
    private var repositoryInstance: EquipmentRepository? = null

    val equipmentRepository: EquipmentRepository
        get() = checkNotNull(repositoryInstance) {
            "AppContainer.initialize(context) must be called before accessing repositories"
        }

    fun initialize(context: Context) {
        if (repositoryInstance != null) return
        synchronized(this) {
            if (repositoryInstance == null) {
                repositoryInstance = RoomEquipmentRepository(
                    database = InventoryDatabase.getInstance(context.applicationContext),
                    inventoryApi = NetworkModule.api
                )
            }
        }
    }
}
