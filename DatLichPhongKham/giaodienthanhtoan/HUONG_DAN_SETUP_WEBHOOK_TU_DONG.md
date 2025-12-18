# 🔄 Hướng dẫn Setup Webhook Tự Động từ Casso

## Vấn đề hiện tại
Bạn phải **bấm nút "Xác nhận thanh toán" thủ công** vì webhook từ Casso chưa được cấu hình hoặc chưa hoạt động.

## Giải pháp: Setup Webhook Tự Động

### Bước 1: Chạy ngrok để expose server
```bash
# Mở terminal và chạy:
ngrok http 8080
```

Sau khi chạy, bạn sẽ nhận được URL HTTPS, ví dụ:
```
Forwarding: https://abc123.ngrok.io -> http://localhost:8080
```

**Lưu ý**: URL này sẽ thay đổi mỗi lần chạy lại ngrok. Nếu muốn URL cố định, cần đăng ký ngrok account và dùng reserved domain.

### Bước 2: Cấu hình Webhook trên Casso

1. **Truy cập trang cấu hình Webhook**
   - Link trực tiếp: https://flow.casso.vn/business/14610/apps/overview?name=webhook
   - Hoặc đăng nhập Casso → Vào phần **Tích hợp** → **Webhook**

2. **Tạo hoặc chỉnh sửa Webhook**

3. **Tạo hoặc chỉnh sửa Webhook**
   - **URL Webhook**: `https://your-ngrok-url.ngrok.io/api/payments/casso-webhook`
     - Thay `your-ngrok-url` bằng URL ngrok của bạn (ví dụ: `abc123.ngrok.io`)
   - **Method**: `POST`
   - **Secret Key** (nếu có): `phongkham2024_secret_key_abc123xyz789`
     - Secret key này được cấu hình trong `application.properties`
   - **Trạng thái**: ✅ **BẬT/KÍCH HOẠT** (Enabled/Active)

4. **Liên kết với tài khoản ngân hàng**
   - Đảm bảo tài khoản MB Bank (`0363159912`) đã được liên kết với Casso
   - Tài khoản phải ở trạng thái **"Hoạt động"** và **"Đồng bộ"**

5. **Kiểm tra điều kiện kích hoạt**
   - Một số Casso có filter theo số tiền hoặc nội dung
   - Đảm bảo không có filter nào chặn giao dịch của bạn
   - Nếu có filter, hãy thêm điều kiện: `amount >= 2000` hoặc bỏ filter

### Bước 3: Xử lý bước "Gọi thử và Lưu tích hợp"

⚠️ **QUAN TRỌNG**: Ở bước này, Casso sẽ gửi dữ liệu test mặc định:
- Nội dung: `"giao dich thu nghiem"` (không có appointmentId)
- Số tiền: `599000` (không khớp với 2000)

→ **Sẽ báo lỗi "Không tìm thấy appointmentId trong nội dung chuyển khoản"** - Đây là **BÌNH THƯỜNG**!

#### ✅ Giải pháp: Bỏ qua test và lưu luôn

1. **Bỏ qua bước test** (không cần click "Gọi thử")
2. **Click nút "Lưu thay đổi"** ở góc trên bên phải
3. Webhook sẽ được lưu và **sẽ hoạt động khi có giao dịch thật**

**Lý do**: 
- Dữ liệu test của Casso không có appointmentId là bình thường
- Khi có giao dịch thật, nội dung sẽ có appointmentId (VD: `COCLK19`)
- Webhook sẽ hoạt động đúng với giao dịch thật

#### Hoặc: Test thủ công bằng Postman (nếu muốn test)
```http
POST https://your-ngrok-url.ngrok.io/api/payments/casso-webhook
Content-Type: application/json

{
  "description": "COCLK19",
  "amount": 2000,
  "tid": "TEST123",
  "subAccId": "0363159912",
  "when": "2025-12-14T01:59:22",
  "cusName": "NGUYEN VAN A",
  "secretKey": "phongkham2024_secret_key_abc123xyz789"
}
```

### Bước 4: Test Webhook với giao dịch thật

Sau khi đã lưu webhook (bỏ qua bước test), bạn có thể test với giao dịch thật:

1. **Tạo payment mới** với `paymentMethod: "BANK_TRANSFER"`
2. **Quét QR code** từ `paymentUrl`
3. **Chuyển khoản** với nội dung: `COCLK19` (hoặc `COC_LK_19`) và số tiền: `2000`
4. **Casso sẽ TỰ ĐỘNG gửi webhook** → Hệ thống tự động cập nhật status thành `CONFIRMED`

### Bước 5: Kiểm tra Webhook có hoạt động không

#### Kiểm tra trên ngrok:
1. Mở http://127.0.0.1:4040 (ngrok web UI)
2. Tab **Requests** → Xem có request nào từ Casso không
3. Nếu **KHÔNG CÓ** → Webhook chưa được gửi từ Casso

#### Kiểm tra log Spring Boot:
Nếu webhook đến server, bạn sẽ thấy trong console:
```
=== CASSO WEBHOOK RECEIVED ===
Headers - X-Secret-Key: ...
Headers - X-Casso-Signature: ...
Body: {...}
Parsed description: COCLK19
Parsed amount: 2000.0
Created callbackDTO: ...
```

#### Kiểm tra trên Casso Dashboard:
1. Vào **Tích hợp** → **Webhook** → **Lịch sử webhook**
2. Xem có webhook nào được gửi không
3. Status: Thành công/Thất bại
4. Nếu thất bại → Xem lỗi cụ thể

### Bước 5: Replay Webhook cho giao dịch đã chuyển khoản

Nếu bạn đã chuyển khoản nhưng webhook chưa được gửi:

1. **Vào Casso → Lịch sử giao dịch**
2. **Tìm giao dịch** bạn vừa chuyển khoản (ví dụ: "COCLK19" với số tiền 2,000)
3. **Click vào giao dịch** để xem chi tiết
4. **Tìm nút "Gửi lại webhook"** hoặc **"Replay webhook"** hoặc **"Đồng bộ lại"**
5. **Click để gửi lại webhook** → Kiểm tra ngrok và log Spring Boot

## ⚠️ Lưu ý quan trọng

1. **Ngrok URL thay đổi**: Mỗi lần chạy lại ngrok, URL sẽ thay đổi → Cần cập nhật lại trên Casso

2. **Độ trễ webhook**: Một số giao dịch có thể mất vài phút để Casso đồng bộ và gửi webhook

3. **Production**: Khi deploy lên server thật, cần:
   - Dùng domain cố định (không phải ngrok)
   - Cấu hình HTTPS
   - Cập nhật webhook URL trên Casso thành: `https://your-domain.com/api/payments/casso-webhook`

4. **Secret Key**: Đảm bảo secret key trên Casso khớp với `casso.webhook.secret-key` trong `application.properties`

## ✅ Kết quả mong đợi

Sau khi setup xong, khi có giao dịch chuyển khoản:
- ✅ Casso **TỰ ĐỘNG** gửi webhook đến server
- ✅ Server **TỰ ĐỘNG** parse nội dung chuyển khoản
- ✅ Payment status **TỰ ĐỘNG** chuyển từ `PENDING` → `CONFIRMED`
- ✅ Appointment status **TỰ ĐỘNG** chuyển từ `PENDING`/`AWAITING_DEPOSIT` → `CONFIRMED`
- ✅ **KHÔNG CẦN** bấm nút "Xác nhận thanh toán" thủ công nữa!

## 🔍 Troubleshooting

### Webhook không tự động gửi:
- ✅ Kiểm tra webhook đã được **kích hoạt** trên Casso chưa
- ✅ Kiểm tra tài khoản ngân hàng đã **liên kết** với Casso chưa
- ✅ Kiểm tra webhook URL đã **đúng** chưa (có `/api/payments/casso-webhook` ở cuối)
- ✅ Kiểm tra ngrok đang **chạy** và URL còn **hoạt động** không

### Webhook gửi nhưng lỗi:
- ✅ Kiểm tra secret key đã **khớp** chưa
- ✅ Kiểm tra payload format đã **đúng** chưa (xem log Spring Boot)
- ✅ Kiểm tra nội dung chuyển khoản có **chứa appointmentId** không (VD: `COCLK19`)

### Vẫn phải xác nhận thủ công:
- ✅ Dùng nút **"Replay webhook"** trên Casso cho giao dịch cụ thể
- ✅ Hoặc dùng giao diện test (`index.html`) để xác nhận thủ công (tạm thời)

