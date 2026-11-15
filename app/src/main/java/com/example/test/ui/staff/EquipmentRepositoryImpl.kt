package com.example.test.ui.staff

class EquipmentRepositoryImpl : EquipmentRepository {
    override suspend fun getAll(): List<Equipment> {
        return  listOf(
            Equipment(1, "Modem Quang iGate", "MDM-001", "Cái", 50),
            Equipment(2, "Switch 8-Port", "SWT-008", "Cái", 20),
            Equipment(3, "Kìm bấm mạng", "KIM-001", "Bộ", 10)
        )
    }
}
