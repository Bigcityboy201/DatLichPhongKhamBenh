## Hướng dẫn test webhook Casso (BANK_TRANSFER)

### 1. Chuẩn bị

- Chạy backend ở `localhost:8080`
- Chạy ngrok: `ngrok http 8080` → lấy URL HTTPS, ví dụ `https://abc123.ngrok.io`
- **Cấu hình Casso webhook để tự động gửi:**
  1. Đăng nhập Casso → **Tích hợp** → **Webhook**
  2. Tạo webhook mới hoặc chỉnh sửa webhook hiện có:
     - **URL**: `https://abc123.ngrok.io/api/payments/casso-webhook`
     - **Method**: `POST`
     - **Secret Key** (nếu có): `phongkham2024_secret_key_abc123xyz789`
     - **Trạng thái**: ✅ **BẬT/KÍCH HOẠT** (Enabled/Active)
  3. **Quan trọng**: Đảm bảo webhook được **kích hoạt** và **liên kết với tài khoản ngân hàng** của bạn
  4. Kiểm tra **Điều kiện kích hoạt**: Casso sẽ gửi webhook khi:
     - Có giao dịch chuyển khoản đến tài khoản đã liên kết
     - Giao dịch khớp với điều kiện (nếu có filter)

### 2. Tạo payment chờ cọc

- Request:
  - POST `http://localhost:8080/api/payments`
  - Headers: `Content-Type: application/json`, `Authorization: Bearer <token>`
  - Body:
    ```json
    { "appointmentId": 13, "paymentMethod": "BANK_TRANSFER" }
    ```
- Kết quả: payment `PENDING`, amount `2000`, paymentUrl (QR MB Bank), transactionId `BANK_13_...`

### 3. Test webhook

#### Cách A: Dùng Postman (chắc chắn nhất)

- POST `https://abc123.ngrok.io/api/payments/casso-webhook`
- Headers: `Content-Type: application/json`
- Body mẫu (khớp business):
  ```json
  {
    "description": "COC_LK_13",
    "amount": 2000,
    "tid": "TEST123",
    "subAccId": "9704xxxx1234",
    "when": "2025-12-12T10:00:00",
    "cusName": "NGUYEN VAN A",
    "secretKey": "phongkham2024_secret_key_abc123xyz789"
  }
  ```

#### Cách B: “Gọi thử” trên Casso (KHÔNG KHUYẾN NGHỊ)

- ⚠️ Payload test mặc định của Casso:
  ```json
  {
    "description": "giao dich thu nghiem", // ❌ Không chứa appointmentId
    "amount": 599000 // ❌ Không khớp 2000
  }
  ```
- ❌ Sẽ lỗi: "Không tìm thấy appointmentId trong nội dung chuyển khoản"
- ✅ Để test thành công, phải sửa payload trên Casso thành:
  ```json
  {
    "description": "COC_LK_15", // ✅ Chứa số 15
    "amount": 2000, // ✅ Khớp với payment
    "tid": "TEST123",
    "subAccId": "9704xxxx1234",
    "when": "2025-12-12T10:00:00",
    "cusName": "NGUYEN VAN A"
  }
  ```
- 💡 **KHUYẾN NGHỊ**: Dùng Cách A (Postman) hoặc chuyển khoản thật thay vì "Gọi thử"

### 4. Chuyển khoản thật (QR MB) - Tự động webhook

- Quét `paymentUrl` từ bước 2.
- Nội dung CK: `COC_LK_13` (hoặc `COCLK13`), số tiền: 2000.
- **Casso sẽ TỰ ĐỘNG gửi webhook** → system auto CONFIRMED.

#### ⚠️ Nếu webhook không tự động gửi, kiểm tra:

1. **Webhook đã được kích hoạt chưa?**

   - Vào Casso → Tích hợp → Webhook
   - Đảm bảo trạng thái là **"Bật"** hoặc **"Active"**

2. **Tài khoản ngân hàng đã liên kết với Casso chưa?**

   - Vào Casso → Tài khoản → Kiểm tra tài khoản MB Bank đã được liên kết
   - Đảm bảo tài khoản đang **hoạt động** và **đồng bộ**

3. **Webhook URL đúng chưa?**

   - Kiểm tra URL trên Casso: `https://your-ngrok-url.ngrok.io/api/payments/casso-webhook`
   - ⚠️ **Lưu ý**: Ngrok URL thay đổi mỗi lần chạy lại → Cần cập nhật lại trên Casso

4. **Kiểm tra log trên Casso:**

   - Vào Casso → Tích hợp → Webhook → **Lịch sử webhook**
   - Xem có request nào được gửi không, status là gì (thành công/thất bại)

5. **Test webhook thủ công:**
   - Trên Casso có thể có nút **"Gửi lại"** hoặc **"Replay"** cho giao dịch cụ thể
   - Hoặc dùng Postman để test (xem bước 3)

### 5. Kiểm tra kết quả

- GET `http://localhost:8080/api/payments/{paymentId}` (Auth Bearer)
- Payment status: `CONFIRMED`, appointment: `CONFIRMED`.

### 6. Debug - Webhook không tự động gửi

#### Kiểm tra webhook có được gửi không:

1. **Xem ngrok web UI:**

   - Mở http://127.0.0.1:4040
   - Tab **Requests** → Xem có request nào từ Casso không
   - Nếu **KHÔNG CÓ** → Casso chưa gửi webhook

2. **Xem log Spring Boot:**

   - Nếu có webhook đến sẽ thấy:
     ```
     === CASSO WEBHOOK RECEIVED ===
     Body: {...}
     Parsed description: ...
     ```
   - Nếu **KHÔNG THẤY** log này → Webhook chưa đến server

3. **Kiểm tra trên Casso Dashboard:**
   - Vào **Tích hợp** → **Webhook** → **Lịch sử**
   - Xem có webhook nào được gửi không
   - Status: Thành công/Thất bại
   - Nếu thất bại → Xem lỗi cụ thể

#### Các lỗi thường gặp:

- **"Nội dung chuyển khoản không được bỏ trống"**: Payload không có field description/content/... hoặc rỗng
- **"Số tiền không hợp lệ"**: Amount khác 2000
- **"Không tìm thấy appointmentId"**: Nội dung CK không chứa số appointmentId
- **"Không tìm thấy payment"**: AppointmentId không parse được hoặc payment không ở PENDING

#### Giải pháp nếu webhook không tự động:

**Cách 1: Replay/Gửi lại webhook từ Casso (KHUYẾN NGHỊ)**

1. Vào Casso → **Lịch sử giao dịch** (như hình bạn đã chụp)
2. Tìm giao dịch bạn vừa chuyển khoản (ví dụ: "COCLK15" với số tiền 2,000)
3. Click vào giao dịch đó để xem chi tiết
4. Tìm nút **"Gửi lại webhook"** hoặc **"Replay webhook"** hoặc **"Đồng bộ lại"**
5. Click để gửi lại webhook → Kiểm tra ngrok và log Spring Boot

**Cách 2: Kiểm tra và sửa cấu hình webhook**

1. **Đảm bảo ngrok đang chạy** và URL đúng
2. **Cập nhật webhook URL trên Casso** nếu ngrok URL thay đổi:
   - Vào Casso → **Tích hợp** → **Webhook**
   - Sửa URL thành: `https://your-current-ngrok-url.ngrok.io/api/payments/casso-webhook`
   - Lưu lại
3. **Kiểm tra webhook có được kích hoạt không:**
   - Đảm bảo trạng thái là **"Bật"** hoặc **"Active"**
4. **Kiểm tra điều kiện kích hoạt webhook:**
   - Một số Casso có filter theo số tiền hoặc nội dung
   - Đảm bảo không có filter nào chặn giao dịch của bạn

**Cách 3: Test thủ công bằng Postman (nếu không có nút replay)**

- Copy thông tin từ giao dịch trên Casso:
  - Mô tả: `COCLK15` (hoặc `COC_LK_15`)
  - Số tiền: `2000`
  - Mã GD: (tid từ Casso)
- Gửi POST đến ngrok URL với payload đúng format (xem bước 3 - Cách A)

**Cách 4: Kiểm tra lịch sử webhook trên Casso**

1. Vào Casso → **Tích hợp** → **Webhook** → **Lịch sử webhook**
2. Xem có webhook nào được gửi cho giao dịch này không
3. Nếu có nhưng **thất bại** → Xem lỗi cụ thể và sửa
4. Nếu **không có** → Webhook chưa được kích hoạt tự động

**Lưu ý quan trọng:**

- ⚠️ Ngrok URL thay đổi mỗi lần chạy lại → Cần cập nhật lại trên Casso
- ⚠️ Một số giao dịch có thể mất vài phút để Casso đồng bộ và gửi webhook
- ✅ **Cách nhanh nhất**: Dùng nút "Replay/Gửi lại webhook" trên Casso cho giao dịch cụ thể
