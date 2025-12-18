# Giao Diện Test Thanh Toán

Giao diện này giúp bạn test và kiểm tra trạng thái thanh toán chuyển khoản.

## Cách sử dụng

### 1. Mở giao diện
- Mở file `index.html` trong trình duyệt
- Hoặc serve qua web server (nếu cần)

### 2. Xác nhận thanh toán thủ công

Khi bạn đã chuyển khoản thành công nhưng hệ thống chưa nhận được webhook từ Casso, bạn có thể xác nhận thủ công:

1. **Nhập API URL**: Mặc định là `http://localhost:8080/api/payments/bank-transfer-callback`
   - Nếu server chạy ở port khác, sửa lại cho đúng

2. **Nhập nội dung chuyển khoản**: 
   - Format có dấu gạch dưới: `COC_LK_19`
   - Format không có dấu gạch dưới: `COCLK19` (như trong ảnh BIDV)
   - Hệ thống hỗ trợ cả hai format

3. **Nhập số tiền**: VD: `2000`

4. **Nhập thông tin khác** (tùy chọn):
   - Số tài khoản người chuyển
   - Tên người chuyển
   - Mã giao dịch ngân hàng

5. **Click "Xác nhận thanh toán"**

### 3. Kiểm tra trạng thái thanh toán

Để kiểm tra trạng thái của một payment:

1. **Nhập Payment ID**: ID của payment cần kiểm tra
2. **Nhập JWT Token** (nếu cần): Token để xác thực
3. **Click "Kiểm tra trạng thái"**

### 4. Xem thanh toán theo Appointment

Để xem tất cả thanh toán của một appointment:

1. **Nhập Appointment ID**: VD: `19`
2. **Nhập JWT Token** (nếu cần)
3. **Click "Xem thanh toán"**

## Lưu ý

- **Endpoint `/api/payments/bank-transfer-callback`** không cần JWT token (public endpoint)
- **Endpoint `/api/payments/{id}/status`** và `/api/payments/appointment/{id}` cần JWT token nếu có authentication

## Ví dụ test với dữ liệu thực tế

Dựa trên ảnh BIDV bạn đã gửi:
- **Nội dung**: `COCLK19`
- **Số tiền**: `2000`
- **Mã giao dịch**: `020097048812140159212025eo8h810965`
- **Appointment ID**: `19`

Bạn có thể test bằng cách:
1. Mở giao diện
2. Nhập các thông tin trên vào form "Xác nhận thanh toán chuyển khoản"
3. Click "Xác nhận thanh toán"
4. Kiểm tra kết quả

## Troubleshooting

### Lỗi 400: "Không tìm thấy appointmentId trong nội dung chuyển khoản"
- Kiểm tra lại nội dung chuyển khoản đã nhập đúng chưa
- Đảm bảo format là `COCLK19` hoặc `COC_LK_19`
- Kiểm tra có khoảng trắng thừa không

### Lỗi 404: "Payment not found"
- Kiểm tra Payment ID đã đúng chưa
- Kiểm tra appointment có payment nào chưa

### Lỗi kết nối
- Kiểm tra API URL đã đúng chưa
- Kiểm tra server đang chạy chưa
- Kiểm tra CORS nếu gọi từ domain khác



