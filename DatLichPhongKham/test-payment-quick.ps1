# Script PowerShell để test nhanh Payment API
# Sử dụng: .\test-payment-quick.ps1

$baseUrl = "http://localhost:8080"
$username = Read-Host "Nhập username"
$password = Read-Host "Nhập password" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

Write-Host "`n=== ĐĂNG NHẬP ===" -ForegroundColor Cyan
$loginBody = @{
    userName = $username
    password = $passwordPlain
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/signIn" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody

$token = $loginResponse.data.token
Write-Host "✅ Đăng nhập thành công!" -ForegroundColor Green
Write-Host "Token: $($token.Substring(0, 50))..." -ForegroundColor Gray

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Menu
Write-Host "`n=== MENU TEST ===" -ForegroundColor Cyan
Write-Host "1. Tạo payment MOMO"
Write-Host "2. Tạo payment BANK_TRANSFER"
Write-Host "3. Tạo payment CASH"
Write-Host "4. Lấy QR Code đặt cọc"
Write-Host "5. Lấy QR Code tùy chỉnh"
Write-Host "6. Xem danh sách payments của tôi"
Write-Host "7. Kiểm tra trạng thái payment"
Write-Host "8. Test Bank Transfer Callback"
Write-Host "0. Thoát"

$choice = Read-Host "`nChọn chức năng (0-8)"

switch ($choice) {
    "1" {
        Write-Host "`n=== TẠO PAYMENT MOMO ===" -ForegroundColor Cyan
        $appointmentId = Read-Host "Nhập appointmentId"
        $amount = Read-Host "Nhập số tiền (VNĐ)"
        
        $body = @{
            appointmentId = [int]$appointmentId
            amount = [double]$amount
            paymentMethod = "MOMO"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/api/payments" `
            -Method POST `
            -Headers $headers `
            -Body $body
        
        Write-Host "✅ Tạo payment thành công!" -ForegroundColor Green
        Write-Host "Payment ID: $($response.data.id)" -ForegroundColor Yellow
        Write-Host "Status: $($response.data.status)" -ForegroundColor Yellow
        Write-Host "Payment URL: $($response.data.paymentUrl)" -ForegroundColor Yellow
    }
    
    "2" {
        Write-Host "`n=== TẠO PAYMENT BANK_TRANSFER ===" -ForegroundColor Cyan
        $appointmentId = Read-Host "Nhập appointmentId"
        $amount = Read-Host "Nhập số tiền (VNĐ)"
        
        $body = @{
            appointmentId = [int]$appointmentId
            amount = [double]$amount
            paymentMethod = "BANK_TRANSFER"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/api/payments" `
            -Method POST `
            -Headers $headers `
            -Body $body
        
        Write-Host "✅ Tạo payment thành công!" -ForegroundColor Green
        Write-Host "Payment ID: $($response.data.id)" -ForegroundColor Yellow
        Write-Host "Status: $($response.data.status)" -ForegroundColor Yellow
        Write-Host "QR Code URL: $($response.data.paymentUrl)" -ForegroundColor Yellow
        Write-Host "Nội dung chuyển khoản: COC_LK_$appointmentId" -ForegroundColor Yellow
    }
    
    "3" {
        Write-Host "`n=== TẠO PAYMENT CASH ===" -ForegroundColor Cyan
        $appointmentId = Read-Host "Nhập appointmentId"
        $amount = Read-Host "Nhập số tiền (VNĐ)"
        
        $body = @{
            appointmentId = [int]$appointmentId
            amount = [double]$amount
            paymentMethod = "CASH"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$baseUrl/api/payments" `
            -Method POST `
            -Headers $headers `
            -Body $body
        
        Write-Host "✅ Tạo payment thành công!" -ForegroundColor Green
        Write-Host "Payment ID: $($response.data.id)" -ForegroundColor Yellow
        Write-Host "Status: $($response.data.status)" -ForegroundColor Yellow
    }
    
    "4" {
        Write-Host "`n=== LẤY QR CODE ĐẶT CỌC ===" -ForegroundColor Cyan
        $appointmentId = Read-Host "Nhập appointmentId (Enter để bỏ qua)"
        $paymentMethod = Read-Host "Nhập paymentMethod (TIMO/BANK_TRANSFER, mặc định: TIMO)"
        
        if ([string]::IsNullOrWhiteSpace($paymentMethod)) {
            $paymentMethod = "TIMO"
        }
        
        $url = "$baseUrl/api/qrcode/deposit?paymentMethod=$paymentMethod"
        if (![string]::IsNullOrWhiteSpace($appointmentId)) {
            $url += "&appointmentId=$appointmentId"
        }
        
        $response = Invoke-RestMethod -Uri $url `
            -Method GET `
            -Headers $headers
        
        Write-Host "✅ Lấy QR Code thành công!" -ForegroundColor Green
        Write-Host "QR Code URL: $($response.data.qrCodeUrl)" -ForegroundColor Yellow
        Write-Host "Số tiền: $($response.data.amount) VNĐ" -ForegroundColor Yellow
        Write-Host "Nội dung: $($response.data.content)" -ForegroundColor Yellow
        Write-Host "Số tài khoản: $($response.data.accountNumber)" -ForegroundColor Yellow
    }
    
    "5" {
        Write-Host "`n=== LẤY QR CODE TÙY CHỈNH ===" -ForegroundColor Cyan
        $appointmentId = Read-Host "Nhập appointmentId (Enter để bỏ qua)"
        $amount = Read-Host "Nhập số tiền (VNĐ, mặc định: 2000)"
        $paymentMethod = Read-Host "Nhập paymentMethod (TIMO/BANK_TRANSFER, mặc định: TIMO)"
        
        if ([string]::IsNullOrWhiteSpace($amount)) {
            $amount = "2000"
        }
        if ([string]::IsNullOrWhiteSpace($paymentMethod)) {
            $paymentMethod = "TIMO"
        }
        
        $url = "$baseUrl/api/qrcode?paymentMethod=$paymentMethod&amount=$amount"
        if (![string]::IsNullOrWhiteSpace($appointmentId)) {
            $url += "&appointmentId=$appointmentId"
        }
        
        $response = Invoke-RestMethod -Uri $url `
            -Method GET `
            -Headers $headers
        
        Write-Host "✅ Lấy QR Code thành công!" -ForegroundColor Green
        Write-Host "QR Code URL: $($response.data.qrCodeUrl)" -ForegroundColor Yellow
        Write-Host "Số tiền: $($response.data.amount) VNĐ" -ForegroundColor Yellow
        Write-Host "Nội dung: $($response.data.content)" -ForegroundColor Yellow
    }
    
    "6" {
        Write-Host "`n=== DANH SÁCH PAYMENTS CỦA TÔI ===" -ForegroundColor Cyan
        $page = Read-Host "Nhập page (mặc định: 0)"
        $size = Read-Host "Nhập size (mặc định: 10)"
        
        if ([string]::IsNullOrWhiteSpace($page)) { $page = "0" }
        if ([string]::IsNullOrWhiteSpace($size)) { $size = "10" }
        
        $response = Invoke-RestMethod -Uri "$baseUrl/api/payments/me?page=$page&size=$size" `
            -Method GET `
            -Headers $headers
        
        Write-Host "✅ Lấy danh sách thành công!" -ForegroundColor Green
        Write-Host "Tổng số: $($response.data.totalElements) payments" -ForegroundColor Yellow
        Write-Host "Trang hiện tại: $($response.data.currentPage + 1)/$($response.data.totalPages)" -ForegroundColor Yellow
        
        foreach ($payment in $response.data.content) {
            Write-Host "`n--- Payment ID: $($payment.id) ---" -ForegroundColor Cyan
            Write-Host "  Số tiền: $($payment.amount) VNĐ"
            Write-Host "  Phương thức: $($payment.paymentMethod)"
            Write-Host "  Trạng thái: $($payment.status)"
            Write-Host "  Appointment ID: $($payment.appointmentId)"
        }
    }
    
    "7" {
        Write-Host "`n=== KIỂM TRA TRẠNG THÁI PAYMENT ===" -ForegroundColor Cyan
        $paymentId = Read-Host "Nhập paymentId"
        
        $response = Invoke-RestMethod -Uri "$baseUrl/api/payments/$paymentId/status" `
            -Method GET `
            -Headers $headers
        
        Write-Host "✅ Kiểm tra thành công!" -ForegroundColor Green
        Write-Host "Payment ID: $($response.data.id)" -ForegroundColor Yellow
        Write-Host "Status: $($response.data.status)" -ForegroundColor Yellow
        Write-Host "Amount: $($response.data.amount) VNĐ" -ForegroundColor Yellow
        Write-Host "Payment Method: $($response.data.paymentMethod)" -ForegroundColor Yellow
    }
    
    "8" {
        Write-Host "`n=== TEST BANK TRANSFER CALLBACK ===" -ForegroundColor Cyan
        $appointmentId = Read-Host "Nhập appointmentId"
        $amount = Read-Host "Nhập số tiền (VNĐ)"
        $fromAccount = Read-Host "Nhập số tài khoản người gửi (Enter để bỏ qua)"
        $fromName = Read-Host "Nhập tên người gửi (Enter để bỏ qua)"
        $bankTransactionId = Read-Host "Nhập mã giao dịch ngân hàng (Enter để bỏ qua)"
        
        $body = @{
            content = "COC_LK_$appointmentId"
            amount = [double]$amount
        }
        
        if (![string]::IsNullOrWhiteSpace($fromAccount)) {
            $body.fromAccount = $fromAccount
        }
        if (![string]::IsNullOrWhiteSpace($fromName)) {
            $body.fromName = $fromName
        }
        if (![string]::IsNullOrWhiteSpace($bankTransactionId)) {
            $body.bankTransactionId = $bankTransactionId
        }
        
        $bodyJson = $body | ConvertTo-Json
        
        # Không cần token cho webhook
        $webhookHeaders = @{
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$baseUrl/api/payments/bank-transfer-callback" `
            -Method POST `
            -Headers $webhookHeaders `
            -Body $bodyJson
        
        Write-Host "✅ Callback thành công!" -ForegroundColor Green
        Write-Host "Payment ID: $($response.data.id)" -ForegroundColor Yellow
        Write-Host "Status: $($response.data.status)" -ForegroundColor Yellow
        Write-Host "Amount: $($response.data.amount) VNĐ" -ForegroundColor Yellow
    }
    
    "0" {
        Write-Host "`nTạm biệt!" -ForegroundColor Cyan
        exit
    }
    
    default {
        Write-Host "Lựa chọn không hợp lệ!" -ForegroundColor Red
    }
}

Write-Host "`nNhấn Enter để tiếp tục..."
Read-Host


