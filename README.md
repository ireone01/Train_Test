# VNPT Inventory

Ứng dụng Android Kotlin + Jetpack Compose mô phỏng quy trình mượn và cấp phát thiết bị. Bản demo có một REST API giả chạy qua HTTP loopback trong app; Retrofit gọi API và Room lưu cache cho UI.

## Công nghệ

- Kotlin, Jetpack Compose, MVVM, StateFlow, Coroutines
- Retrofit 3.0.0 và Gson cho HTTP/JSON
- Room 2.8.5 làm cache SQLite cục bộ
- MockWebServer (chỉ debug) mở fake HTTP API trên `127.0.0.1` với cổng ngẫu nhiên

## Chạy ứng dụng

Mở project trong Android Studio và chạy cấu hình `app` trên Emulator hoặc thiết bị Android. Có thể build APK debug:

```powershell
.\gradlew.bat :app:assembleDebug
```

Tài khoản demo: Staff `0912345678`, Manager `0988888888`, Storekeeper `0905111222`; mật khẩu đều là `123`.

## API contract

Base URL khi chạy debug có dạng `http://127.0.0.1:<port>/api/`. Cổng được cấp tự động. Các endpoint hiện được khai báo trong `InventoryApi.kt` và dispatcher trong `src/debug/.../FakeApiServer.kt`:

| Method | Path | Body | Thành công |
|---|---|---|---|
| `POST` | `/auth/login` | `{ "phone", "password" }` | phone, role, demo access token |
| `GET` | `/equipment` | — | danh sách thiết bị và tồn kho |
| `GET` | `/requests` | — | toàn bộ phiếu |
| `POST` | `/requests` | `{ "staffPhone", "equipmentId", "quantity" }` | phiếu `PENDING_MANAGER` |
| `PATCH` | `/requests/{id}/approve` | — | phiếu `PENDING_STOREKEEPER` |
| `POST` | `/requests/{id}/issue` | — | phiếu `WAITING_CONFIRMATION` và OTP 6 số |
| `POST` | `/requests/{id}/confirm` | `{ "staffPhone", "otp" }` | phiếu `COMPLETED` và tồn kho mới |

Các endpoint thao tác phiếu nhận header `X-Actor-Role` để mock server kiểm tra vai trò. Đây chỉ là cách giả lập phân quyền cho bài học; backend thật phải xác thực access token và tự xác định vai trò, không tin vai trò do client gửi.

## Luồng dữ liệu

```text
Compose -> ViewModel -> Repository -> Retrofit -> HTTP loopback -> FakeApiServer
                              ^                               |
                              +---- Room cache <- DTO/JSON ---+
```

Khi khởi động, Repository tải catalog và phiếu từ API rồi đồng bộ vào Room. Các lệnh tạo, duyệt, xuất và xác nhận gọi API trước; chỉ khi server trả thành công, Repository mới cập nhật cache Room trong transaction. UI quan sát Room bằng Flow.

Mock server lưu trạng thái giả lập vào SharedPreferences để dữ liệu demo giữ được sau khi app process khởi động lại. Stock chỉ giảm khi OTP đúng; OTP sai, vai trò sai, trạng thái không hợp lệ hoặc tồn kho không đủ sẽ trả HTTP lỗi và không cập nhật cache. Xóa app data để reset mock server và Room về dữ liệu ban đầu.

## Giới hạn của bản demo

- Fake server chỉ có trong debug build, chạy cùng app; không phải backend có thể truy cập từ máy khác và không cần Internet.
- Đăng nhập, token và header vai trò chỉ phục vụ mô phỏng, không có bảo mật thật.
- Base URL HTTP loopback và `usesCleartextTraffic` chỉ được bật trong debug build. Release build dùng URL placeholder `https://api.example.invalid/api/`; cần thay bằng domain thật.
- API contract được tự giả định vì chưa có đặc tả endpoint chính thức.

## Room

Database tên `vnpt_inventory.db`; schema Room được lưu tại `app/schemas`. Bảng phiếu có foreign key tới thiết bị và index cho các truy vấn phổ biến. Room giữ cache và phát Flow cho UI; mock API đang đóng vai trò backend authoritative trong bản mô phỏng.
