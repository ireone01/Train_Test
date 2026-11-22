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

## Notification và alarm

Sau khi đăng nhập bằng bất kỳ vai trò nào, nhấn **Nhắc việc** ở thanh trên cùng để mở hộp thoại demo. Tính năng này đứng độc lập với quy trình mượn thiết bị.

- **Gửi thử** tạo notification cục bộ ngay lập tức.
- Nhập giờ 24 giờ theo dạng `HH:mm`, ví dụ `14:30`, rồi nhấn **Đặt alarm** để lên lịch một notification tại lần xuất hiện kế tiếp của giờ đó. Nếu giờ hôm nay đã qua, alarm sẽ đặt cho ngày mai.
- **Hủy alarm** hủy alarm đang chờ.
- Notification có channel `inventory_reminders`; nhấn vào notification sẽ mở lại ứng dụng.
- Alarm là one-shot, dùng `AlarmManager.setExactAndAllowWhileIdle()` và `RTC_WAKEUP`. Khi tới giờ, `NotificationAlarmReceiver` nhận broadcast rồi gửi notification.

### Quyền cần cấp

- Android 13 trở lên: app xin runtime permission `POST_NOTIFICATIONS`. Nếu từ chối thì Android không hiển thị notification.
- Android 12 trở lên: exact alarm có thể cần quyền đặc biệt **Alarms & reminders**. Hộp thoại có nút mở trang Settings; khi quay lại app sẽ thử đặt alarm lại.
- Quyền exact alarm được khai báo bằng `SCHEDULE_EXACT_ALARM`. Cleartext HTTP được bật riêng trong debug manifest để Retrofit gọi MockWebServer loopback; release không bật quyền cleartext này.

Alarm không lặp lại, chưa được khôi phục sau reboot và không có âm thanh/full-screen như ứng dụng đồng hồ báo thức. Hệ điều hành hoặc chính sách tiết kiệm pin của nhà sản xuất vẫn có thể ảnh hưởng thời điểm thực thi. Đây là ví dụ local notification/alarm để trình diễn Android APIs.

## Giới hạn của bản demo

- Fake server chỉ có trong debug build, chạy cùng app; không phải backend có thể truy cập từ máy khác và không cần Internet.
- Đăng nhập, token và header vai trò chỉ phục vụ mô phỏng, không có bảo mật thật.
- Base URL HTTP loopback và `usesCleartextTraffic` chỉ được bật trong debug build. Release build dùng URL placeholder `https://api.example.invalid/api/`; cần thay bằng domain thật.
- API contract được tự giả định vì chưa có đặc tả endpoint chính thức.
- Notification/alarm là ví dụ phía client, không gắn với thông báo nghiệp vụ hay lịch nhắc trên server.

## Room

Database tên `vnpt_inventory.db`; schema Room được lưu tại `app/schemas`. Bảng phiếu có foreign key tới thiết bị và index cho các truy vấn phổ biến. Room giữ cache và phát Flow cho UI; mock API đang đóng vai trò backend authoritative trong bản mô phỏng.
