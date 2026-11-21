package com.example.test.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.test.data.local.dao.BorrowRequestDao
import com.example.test.data.local.dao.EquipmentDao
import com.example.test.data.local.converter.RequestStatusConverter
import com.example.test.data.local.entity.BorrowRequestEntity
import com.example.test.data.local.entity.EquipmentEntity

@Database(
    entities = [EquipmentEntity::class, BorrowRequestEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(RequestStatusConverter::class)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun equipmentDao(): EquipmentDao
    abstract fun borrowRequestDao(): BorrowRequestDao

    companion object {
        private const val DATABASE_NAME = "vnpt_inventory.db"

        @Volatile
        private var instance: InventoryDatabase? = null

        fun getInstance(context: Context): InventoryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
    }
}
