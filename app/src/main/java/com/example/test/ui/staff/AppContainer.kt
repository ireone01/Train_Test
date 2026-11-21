package com.example.test.ui.staff

/** Simple manual dependency container; keeps the fake inventory shared across destinations. */
object AppContainer {
    val equipmentRepository: EquipmentRepository = EquipmentRepositoryImpl()
}
