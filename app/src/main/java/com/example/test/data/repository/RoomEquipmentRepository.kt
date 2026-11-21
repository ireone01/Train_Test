package com.example.test.data.repository

import androidx.room.withTransaction
import com.example.test.data.local.InventoryDatabase
import com.example.test.data.local.entity.BorrowRequestEntity
import com.example.test.data.local.entity.toDomain
import com.example.test.data.local.entity.toEntity
import com.example.test.domain.model.BorrowRequest
import com.example.test.domain.model.Equipment
import com.example.test.domain.model.OperationResult
import com.example.test.domain.model.RequestStatus
import com.example.test.domain.repository.EquipmentRepository
import com.example.test.ui.model.UserRole
import java.security.SecureRandom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomEquipmentRepository(
    private val database: InventoryDatabase
) : EquipmentRepository {
    private val equipmentDao = database.equipmentDao()
    private val requestDao = database.borrowRequestDao()
    private val secureRandom = SecureRandom()

    override fun observeEquipment(): Flow<List<Equipment>> =
        equipmentDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeRequests(): Flow<List<BorrowRequest>> =
        requestDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun initialize() {
        database.withTransaction { seedEquipmentIfEmpty() }
    }

    override suspend fun createRequest(
        actorRole: UserRole,
        staffPhone: String,
        equipmentId: Int,
        quantity: Int
    ): OperationResult = database.withTransaction {
        seedEquipmentIfEmpty()
        if (actorRole != UserRole.STAFF) return@withTransaction failure("Chỉ nhân viên được tạo phiếu")
        if (staffPhone.isBlank()) return@withTransaction failure("Thiếu số điện thoại nhân viên")
        if (quantity <= 0) return@withTransaction failure("Số lượng phải lớn hơn 0")

        val item = equipmentDao.findById(equipmentId)
            ?: return@withTransaction failure("Không tìm thấy thiết bị")
        val available = availableStock(item.id, item.stock)
        if (quantity > available) {
            return@withTransaction failure("Chỉ còn $available ${item.unit} khả dụng")
        }

        val request = BorrowRequestEntity(
            staffPhone = staffPhone,
            equipmentId = item.id,
            equipmentName = item.name,
            equipmentCode = item.code,
            unit = item.unit,
            quantity = quantity,
            status = RequestStatus.PENDING_MANAGER,
            createdAtEpochMillis = System.currentTimeMillis()
        )
        val id = requestDao.insert(request).toInt()
        success("Đã tạo phiếu #$id, đang chờ trưởng đơn vị duyệt")
    }

    override suspend fun approveRequest(
        actorRole: UserRole,
        requestId: Int
    ): OperationResult = database.withTransaction {
        if (actorRole != UserRole.MANAGER) return@withTransaction failure("Chỉ trưởng đơn vị được duyệt phiếu")
        val request = requestDao.findById(requestId)
            ?: return@withTransaction failure("Không tìm thấy phiếu")
        if (request.status != RequestStatus.PENDING_MANAGER) {
            return@withTransaction failure("Phiếu không còn ở trạng thái chờ duyệt")
        }
        requestDao.update(request.copy(status = RequestStatus.PENDING_STOREKEEPER))
        success("Đã duyệt phiếu #${request.id}")
    }

    override suspend fun prepareIssue(
        actorRole: UserRole,
        requestId: Int
    ): OperationResult = database.withTransaction {
        if (actorRole != UserRole.STOREKEEPER) return@withTransaction failure("Chỉ thủ kho được xuất thiết bị")
        val request = requestDao.findById(requestId)
            ?: return@withTransaction failure("Không tìm thấy phiếu")
        if (request.status != RequestStatus.PENDING_STOREKEEPER) {
            return@withTransaction failure("Phiếu không còn ở trạng thái chờ xuất kho")
        }

        val item = equipmentDao.findById(request.equipmentId)
            ?: return@withTransaction failure("Không tìm thấy thiết bị")
        val available = availableStock(item.id, item.stock)
        if (request.quantity > available) {
            return@withTransaction failure("Tồn khả dụng không đủ: còn $available ${item.unit}")
        }

        val otp = secureRandom.nextInt(1_000_000).toString().padStart(6, '0')
        requestDao.update(
            request.copy(
                status = RequestStatus.WAITING_CONFIRMATION,
                otp = otp
            )
        )
        success("Đã chuẩn bị phiếu #${request.id}; OTP bàn giao: $otp")
    }

    override suspend fun confirmReceipt(
        actorRole: UserRole,
        requestId: Int,
        staffPhone: String,
        otp: String
    ): OperationResult = database.withTransaction {
        if (actorRole != UserRole.STAFF) return@withTransaction failure("Chỉ nhân viên được xác nhận nhận hàng")
        val request = requestDao.findById(requestId)
            ?: return@withTransaction failure("Không tìm thấy phiếu")
        if (request.staffPhone != staffPhone) return@withTransaction failure("Phiếu không thuộc nhân viên này")
        if (request.status != RequestStatus.WAITING_CONFIRMATION) {
            return@withTransaction failure("Phiếu chưa sẵn sàng xác nhận hoặc đã hoàn thành")
        }
        if (request.otp != otp) return@withTransaction failure("OTP không chính xác")

        val item = equipmentDao.findById(request.equipmentId)
            ?: return@withTransaction failure("Không tìm thấy thiết bị")
        if (item.stock < request.quantity) {
            return@withTransaction failure("Tồn kho không đủ để hoàn tất phiếu")
        }

        equipmentDao.update(item.copy(stock = item.stock - request.quantity))
        requestDao.update(
            request.copy(
                status = RequestStatus.COMPLETED,
                otp = null
            )
        )
        success("Đã xác nhận nhận hàng; phiếu #${request.id} hoàn thành")
    }

    private suspend fun seedEquipmentIfEmpty() {
        if (equipmentDao.count() == 0) {
            equipmentDao.insertAll(
                listOf(
                    Equipment(1, "Modem Quang iGate", "MDM-001", "Cái", 50),
                    Equipment(2, "Switch 8-Port", "SWT-008", "Cái", 20),
                    Equipment(3, "Kìm bấm mạng", "KIM-001", "Bộ", 10)
                ).map { it.toEntity() }
            )
        }
    }

    private suspend fun availableStock(equipmentId: Int, stock: Int): Int {
        val reserved = requestDao.reservedQuantity(equipmentId, RequestStatus.WAITING_CONFIRMATION)
        return (stock - reserved).coerceAtLeast(0)
    }

    private fun success(message: String) = OperationResult(true, message)
    private fun failure(message: String) = OperationResult(false, message)
}
