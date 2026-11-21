package com.example.test.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.test.data.local.entity.BorrowRequestEntity
import com.example.test.domain.model.RequestStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BorrowRequestDao {
    @Query("SELECT * FROM borrow_requests ORDER BY createdAtEpochMillis DESC, id DESC")
    fun observeAll(): Flow<List<BorrowRequestEntity>>

    @Query("SELECT * FROM borrow_requests WHERE staffPhone = :staffPhone ORDER BY createdAtEpochMillis DESC, id DESC")
    fun observeForStaff(staffPhone: String): Flow<List<BorrowRequestEntity>>

    @Query("SELECT * FROM borrow_requests WHERE status = :status ORDER BY createdAtEpochMillis DESC, id DESC")
    fun observeByStatus(status: RequestStatus): Flow<List<BorrowRequestEntity>>

    @Query("SELECT * FROM borrow_requests WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): BorrowRequestEntity?

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM borrow_requests WHERE equipmentId = :equipmentId AND status = :status")
    suspend fun reservedQuantity(equipmentId: Int, status: RequestStatus): Int

    @Insert
    suspend fun insert(request: BorrowRequestEntity): Long

    @Update
    suspend fun update(request: BorrowRequestEntity): Int
}
