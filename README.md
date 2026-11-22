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

## Hướng dẫn trình diễn toàn bộ nghiệp vụ

Fake API chạy bên trong cùng ứng dụng debug, vì vậy hãy đăng xuất rồi đăng nhập lần lượt bằng từng tài khoản trên cùng một lần cài đặt app. Trạng thái nghiệp vụ được giữ trong SharedPreferences của mock server; Room giữ cache để các màn hình quan sát dữ liệu bằng Flow.

### 1. Nhân viên tạo phiếu

1. Đăng nhập bằng `0912345678` / `123`.
2. Ở tab **Thiết bị**, tìm thiết bị theo tên nếu cần, chọn thiết bị rồi nhập số lượng lớn hơn 0 và không vượt tồn kho đang hiển thị.
3. Nhấn **Tạo phiếu**. Phiếu mới xuất hiện trong tab **Phiếu của tôi** với trạng thái `PENDING_MANAGER`.
4. Thử nhập số lượng 0 hoặc lớn hơn tồn kho để thấy nút tạo phiếu bị vô hiệu hóa. Kiểm tra phiếu không làm thay đổi tồn kho.

### 2. Trưởng đơn vị duyệt phiếu

1. Đăng xuất, đăng nhập bằng `0988888888` / `123`.
2. Màn hình **Trưởng đơn vị — Phiếu chờ duyệt** hiển thị các phiếu `PENDING_MANAGER`.
3. Kiểm tra người tạo, thiết bị và số lượng trên thẻ phiếu, sau đó nhấn **Phê duyệt**.
4. Phiếu chuyển sang `PENDING_STOREKEEPER` và không còn trong danh sách chờ duyệt của trưởng đơn vị.

### 3. Thủ kho chuẩn bị xuất kho và bàn giao OTP

1. Đăng xuất, đăng nhập bằng `0905111222` / `123`.
2. Trong mục **Phiếu chờ xuất kho**, xác nhận tồn hiện tại đủ đáp ứng phiếu.
3. Nhấn **Xuất kho và tạo OTP**. Phiếu chuyển sang `WAITING_CONFIRMATION`; mã OTP sáu chữ số hiển thị trong mục **OTP chờ bàn giao**.
4. Ghi lại OTP để nhập ở bước tiếp theo. Ở bước này tồn kho chưa bị trừ.

### 4. Nhân viên xác nhận đã nhận thiết bị

1. Đăng xuất, đăng nhập lại bằng `0912345678` / `123`.
2. Mở tab **Phiếu của tôi**, tìm phiếu `WAITING_CONFIRMATION` và nhấn **Nhập OTP xác nhận**.
3. Nhập sai OTP để xem lỗi; phiếu vẫn chờ xác nhận và tồn kho không đổi.
4. Nhập đúng OTP thủ kho vừa cung cấp. Phiếu chuyển sang `COMPLETED` và tồn kho giảm đúng số lượng trên phiếu.
5. Đăng nhập lại vai trò thủ kho hoặc nhân viên để kiểm tra trạng thái và tồn kho mới.

Có thể tạo thêm phiếu cho cùng thiết bị để kiểm tra kho thiếu: khi tổng yêu cầu vượt tồn khả dụng, thao tác xuất kho bị từ chối. Thao tác xác nhận lặp lại trên phiếu đã hoàn tất cũng không được trừ kho lần hai.

### 5. Trình diễn nhắc việc

Nút **Nhắc việc** có trên màn hình của cả ba vai trò. **Gửi thử** tạo notification ngay; nhập giờ `HH:mm` rồi chọn **Đặt alarm** để lên lịch một lần vào lần xuất hiện kế tiếp của giờ đó. Cấp quyền notification trên Android 13+ và quyền **Alarms & reminders** nếu thiết bị yêu cầu. Chi tiết và giới hạn được mô tả ở mục *Notification và alarm* phía trên.

## Cấu trúc mã nguồn

Các package chính nằm dưới `app/src/main/java/com/example/test/`:

| Thư mục / tệp | Trách nhiệm |
|---|---|
| `ui/Login/` | Compose đăng nhập, `LoginScreenViewModel` và repository xác thực demo |
| `ui/home/` | Route theo vai trò, màn hình Staff/Manager/Storekeeper và hộp thoại notification/alarm |
| `ui/staff/` | Danh sách thiết bị, thẻ phiếu và ViewModel theo dõi dữ liệu/thao tác phiếu |
| `ui/Route/` | Điều hướng giữa splash, login và nội dung ứng dụng |
| `ui/model/` | Session đăng nhập và enum vai trò |
| `domain/model/` | Model nghiệp vụ thiết bị, phiếu và trạng thái phiếu |
| `domain/repository/` | Hợp đồng repository thiết bị và phiếu |
| `data/repository/RoomEquipmentRepository.kt` | Điều phối API và cache Room; kiểm tra kết quả trước khi cập nhật cache |
| `data/remote/` | Retrofit API, DTO, chuyển đổi lỗi, cấu hình network và interface mock server |
| `data/local/` | Room database, entity, DAO và converter trạng thái |
| `data/remote/FakeApiServer.kt` trong `src/debug/` | Mock HTTP API chỉ được đóng gói trong debug build |
| `di/AppContainer.kt` | Tạo và chia sẻ các dependency chính của ứng dụng |
| `platform/notification/` | Tạo notification, lên/hủy alarm và nhận broadcast khi đến giờ |
| `InventoryApplication.kt` | Khởi tạo thành phần cấp ứng dụng, gồm notification channel |

### Luồng dữ liệu

```text
Compose screen
    -> ViewModel (nhận sự kiện, phát StateFlow)
    -> Repository (quy tắc thao tác và đồng bộ dữ liệu)
    -> Retrofit InventoryApi
    -> FakeApiServer (debug)
    -> response/DTO -> Room transaction -> DAO Flow -> ViewModel -> Compose
```

Màn hình thu thập `StateFlow` từ ViewModel để vẽ lại theo trạng thái. Với dữ liệu nghiệp vụ, Repository gọi API trước; nếu API thành công thì ghi dữ liệu trả về vào Room. Vì vậy API giả là nguồn trạng thái nghiệp vụ của bản demo, còn Room là cache cục bộ phục vụ quan sát dữ liệu. Đăng nhập hiện cũng là mô phỏng theo tài khoản mẫu, không đại diện cho quy trình xác thực bảo mật của hệ thống thật.

## Chạy và kiểm tra nhanh

- Mở thư mục dự án trong Android Studio, chờ Gradle Sync rồi chạy cấu hình `app` trên emulator hoặc thiết bị.
- Tạo/cho phép notification channel khi Android yêu cầu; trên emulator Android 12+ có thể cần bật **Alarms & reminders** trong Settings để thử alarm chính xác.
- Có thể build APK debug bằng `.\gradlew.bat :app:assembleDebug` trên Windows.
- Muốn bắt đầu lại dữ liệu demo, vào Settings của thiết bị, xóa **storage/data** của ứng dụng rồi mở lại. Thao tác này xóa cả dữ liệu mock server và database Room.
- Fake API chỉ có trong debug source set. Bản release không có server giả và URL production hiện là placeholder; muốn chạy release cần cấu hình backend thật.
