package com.example.test.data.remote

import kotlinx.coroutines.delay

/** Simulates GET /equipment latency and payload without requiring a backend or Internet. */
class FakeInventoryApi : InventoryApi {
    override suspend fun getEquipment(): List<EquipmentDto> {
        delay(RESPONSE_DELAY_MILLIS)
        return sampleEquipment
    }

    private companion object {
        const val RESPONSE_DELAY_MILLIS = 350L

        val sampleEquipment = listOf(
            EquipmentDto(1, "Modem Quang iGate", "MDM-001", "Cái", 50),
            EquipmentDto(2, "Switch 8-Port", "SWT-008", "Cái", 20),
            EquipmentDto(3, "Kìm bấm mạng", "KIM-001", "Bộ", 10)
        )
    }
}
