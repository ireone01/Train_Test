package com.example.test.data.local.converter

import androidx.room.TypeConverter
import com.example.test.domain.model.RequestStatus

class RequestStatusConverter {
    @TypeConverter
    fun fromRequestStatus(status: RequestStatus): String = status.name

    @TypeConverter
    fun toRequestStatus(value: String): RequestStatus = RequestStatus.valueOf(value)
}
