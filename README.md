# VNPT Inventory

Ứng dụng Android Kotlin + Jetpack Compose mô phỏng quy trình mượn và cấp phát thiết bị. Bản hiện tại dùng Room lưu dữ liệu cục bộ và có một API giả cho luồng tải danh sách thiết bị để minh họa cách tích hợp API vào ứng dụng.

## Công nghệ

- Kotlin, Jetpack Compose, MVVM, StateFlow
- Room 2.8.5 làm cơ sở dữ liệu cục bộ
- Kotlin Coroutines cho các thao tác bất đồng bộ
- `InventoryApi` là ranh giới giữa Repository và dịch vụ từ xa
- `FakeInventoryApi` mô phỏng `GET /equipment`, có độ trễ 350 ms và không cần Internet

## Chạy ứng dụng

Mở project bằng Android Studio và chạy cấu hình `app` trên Emulator hoặc thiết bị Android. Có thể build APK debug bằng:

```powershell
.\gradlew.bat :app:assembleDebug
```

Tài khoản demo: Staff `0912345678`, Manager `0988888888`, Storekeeper `0905111222`; mật khẩu đều là `123`.

## Luồng API giả đang chạy

Khi khởi động, `InventoryApplication` gọi `EquipmentRepository.initialize()`. Repository gọi `InventoryApi.getEquipment()`; hiện tại dependency được cấu hình là `FakeInventoryApi`. DTO nhận được được chuyển thành Entity và ghi vào Room trong transaction nếu cơ sở dữ liệu chưa có catalog. UI quan sát Flow từ Room, nên vẫn đọc dữ liệu cục bộ sau lần đồng bộ đầu tiên.

```text
InventoryApplication
  -> Repository.initialize()
  -> InventoryApi.getEquipment()
  -> FakeInventoryApi (độ trễ giả lập + danh sách DTO)
  -> DTO sang Domain/Entity
  -> Room
  -> Flow sang ViewModel và Compose UI
```

Đây là API giả trong tiến trình ứng dụng, chưa tạo HTTP request thật. Các thao tác tạo phiếu, duyệt, xuất kho và xác nhận OTP hiện vẫn chạy trực tiếp qua Room repository. Những endpoint dưới đây là hợp đồng minh họa để thay thế từng phần khi có backend hoặc phụ lục API chính thức:

| Method | Endpoint minh họa | Ý nghĩa |
|---|---|---|
| `POST` | `/auth/login` | Đăng nhập, trả access token và vai trò |
| `GET` | `/equipment` | Đọc catalog và tồn kho; đang được mô phỏng trong app |
| `POST` | `/requests` | Tạo phiếu mượn |
| `PATCH` | `/requests/{id}/approve` | Trưởng đơn vị duyệt phiếu |
| `POST` | `/requests/{id}/issue` | Thủ kho chuẩn bị thiết bị, trả OTP demo |
| `POST` | `/requests/{id}/confirm` | Nhân viên gửi OTP để nhận hàng |

## Cơ sở dữ liệu

Database tên `vnpt_inventory.db`. Schema Room được lưu tại `app/schemas`. Entity thiết bị và phiếu mượn nằm riêng; phiếu có foreign key tới thiết bị và index cho các truy vấn thường dùng. Các thay đổi trạng thái và trừ tồn kho qua Room được thực hiện trong transaction.

Room dùng SQLite ở trên thiết bị; SQL vẫn là nền tảng lưu trữ phía dưới Room. Dự án chưa có server, URL API, authentication token thật hoặc tệp SQL backend. Khi có đặc tả chính thức, thay `FakeInventoryApi` bằng implementation Retrofit, bổ sung DTO/error mapping, rồi để Repository đồng bộ response vào Room.

## Trạng thái demo

- Login và phân nhánh UI theo vai trò dùng tài khoản giả lập.
- Staff tạo phiếu; Manager duyệt; Storekeeper xuất và tạo OTP; Staff xác nhận.
- Xác nhận OTP và trừ kho được ghi atomically trong Room.
- `GET /equipment` minh họa quy trình API giả sang Room cache; các mutation chưa gửi qua API.
- Chưa chạy walkthrough trên Emulator.
