# VNPT Inventory

Ứng dụng Android Kotlin + Jetpack Compose mô phỏng quy trình mượn và cấp phát thiết bị. Bản hiện tại dùng Room lưu dữ liệu cục bộ và có một API giả cho luồng tải danh sách thiết bị để minh họa cách tích hợp API vào ứng dụng.

## Công nghệ

- Kotlin, Jetpack Compose, MVVM, StateFlow
- Room 2.8.5 làm cơ sở dữ liệu cục bộ
- Kotlin Coroutines cho các thao tác bất đồng bộ
- Retrofit 3.0.0 cho HTTP client; `InventoryApi` khai báo endpoint
- `FakeApiServer` mở một HTTP server loopback trên cổng ngẫu nhiên trong app process

## Chạy ứng dụng

Mở project bằng Android Studio và chạy cấu hình `app` trên Emulator hoặc thiết bị Android. Có thể build APK debug bằng:

```powershell
.\gradlew.bat :app:assembleDebug
```

Tài khoản demo: Staff `0912345678`, Manager `0988888888`, Storekeeper `0905111222`; mật khẩu đều là `123`.

## Luồng API giả đang chạy

Khi khởi động, `InventoryApplication` gọi `EquipmentRepository.initialize()`. `NetworkModule` mở server giả ở loopback, ví dụ `http://127.0.0.1:49152/api/`, rồi cấu hình Retrofit với URL này. Repository gọi `InventoryApi.getEquipment()` qua HTTP; server đọc request `GET /api/equipment` và trả JSON. Retrofit chuyển JSON thành DTO; repository ánh xạ DTO thành Entity và ghi vào Room trong transaction nếu cơ sở dữ liệu chưa có catalog. UI quan sát Flow từ Room.

```text
InventoryApplication
  -> Repository.initialize()
  -> Retrofit GET http://127.0.0.1:<port>/api/equipment
  -> FakeApiServer trả HTTP 200 + JSON
  -> Gson chuyển JSON thành DTO
  -> DTO sang Entity
  -> Room
  -> Flow sang ViewModel và Compose UI
```

Đây là HTTP request thật qua loopback nhưng server cũng chạy trong app; không gọi backend ngoài và không cần Internet. Cổng được cấp ngẫu nhiên mỗi lần mở process. Có thể xem request và base URL trong Logcat với tag `VNPT-FakeAPI` và `VNPT-Network`. Server giả hiện chỉ trả `GET /api/equipment`. Các thao tác tạo phiếu, duyệt, xuất kho và xác nhận OTP vẫn chạy trực tiếp qua Room repository. Những endpoint dưới đây là hợp đồng minh họa để thay thế từng phần khi có backend hoặc phụ lục API chính thức:

| Method | Endpoint minh họa | Ý nghĩa |
|---|---|---|
| `POST` | `/auth/login` | Đăng nhập, trả access token và vai trò |
| `GET` | `/equipment` | Đọc catalog và tồn kho; đang được phục vụ bằng HTTP local |
| `POST` | `/requests` | Tạo phiếu mượn |
| `PATCH` | `/requests/{id}/approve` | Trưởng đơn vị duyệt phiếu |
| `POST` | `/requests/{id}/issue` | Thủ kho chuẩn bị thiết bị, trả OTP demo |
| `POST` | `/requests/{id}/confirm` | Nhân viên gửi OTP để nhận hàng |

## Cơ sở dữ liệu

Database tên `vnpt_inventory.db`. Schema Room được lưu tại `app/schemas`. Entity thiết bị và phiếu mượn nằm riêng; phiếu có foreign key tới thiết bị và index cho các truy vấn thường dùng. Các thay đổi trạng thái và trừ tồn kho qua Room được thực hiện trong transaction.

Room dùng SQLite ở trên thiết bị; SQL vẫn là nền tảng lưu trữ phía dưới Room. URL hiện tại là URL loopback giả lập, không phải địa chỉ backend có thể dùng bên ngoài thiết bị. Dự án chưa có authentication token thật hoặc SQL backend. Khi có đặc tả chính thức, đổi base URL sang server thật, bổ sung các endpoint, DTO/error mapping, rồi để Repository đồng bộ response vào Room.

## Trạng thái demo

- Login và phân nhánh UI theo vai trò dùng tài khoản giả lập.
- Staff tạo phiếu; Manager duyệt; Storekeeper xuất và tạo OTP; Staff xác nhận.
- Xác nhận OTP và trừ kho được ghi atomically trong Room.
- `GET /api/equipment` đi qua HTTP + Retrofit tới mock server rồi ghi cache Room; các mutation chưa gửi qua API.
- Chưa chạy walkthrough trên Emulator.
