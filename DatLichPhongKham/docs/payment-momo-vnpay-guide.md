## Hướng dẫn tích hợp MoMo & VNPay theo kiến trúc hiện tại

File này chỉ tập trung vào MoMo và VNPay, bám theo code `PaymentServiceIMPL` bạn đang có.

---

### 1. Chuẩn bị chung

- Đăng ký merchant trên **MoMo** và **VNPay**, lấy các thông số:
  - **MoMo**: `partnerCode`, `accessKey`, `secretKey`, `endpoint` (test/prod), `returnUrl`, `notifyUrl`.
  - **VNPay**: `vnp_TmnCode`, `vnp_HashSecret`, `vnp_Url`, `returnUrl`.
- Khai báo trong `application.properties` (hoặc `.yml`):

```properties
# MoMo
payment.momo.partner-code=...
payment.momo.access-key=...
payment.momo.secret-key=...
payment.momo.endpoint=https://test-payment.momo.vn/v2/gateway/api/create
payment.momo.return-url=https://your-frontend.com/payment/momo/return
payment.momo.notify-url=https://your-backend.com/api/payments/momo-callback

# VNPay
payment.vnpay.tmn-code=...
payment.vnpay.hash-secret=...
payment.vnpay.endpoint=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
payment.vnpay.return-url=https://your-frontend.com/payment/vnpay/return
```

---

### 2. Mở rộng enum & request

- Thêm vào `PaymentMethod`:

```java
public enum PaymentMethod {
    CASH,
    BANK_TRANSFER,
    MOMO,
    VNPAY
}
```

- `PaymentRequestDTO.paymentMethod` hiện là `String` → FE gửi: `"CASH" | "BANK_TRANSFER" | "MOMO" | "VNPAY"`.

---

### 3. Tạo interface `PaymentGatewayStrategy`

```java
public interface PaymentGatewayStrategy {

    Payments createPayment(PaymentRequestDTO dto,
                           User currentUser,
                           Appointments appointment,
                           double amount);
}
```

- `amount`: số tiền (hiện bạn đang dùng hằng `DEFAULT_DEPOSIT_AMOUNT`), có thể truyền từ `PaymentServiceIMPL`.

---

### 4. BankTransferStrategy (gói lại code cũ)

```java
@Service
@RequiredArgsConstructor
public class BankTransferPaymentStrategy implements PaymentGatewayStrategy {

    private final QRCodeService qrCodeService;

    @Override
    public Payments createPayment(PaymentRequestDTO dto, User user, Appointments appointment, double amount) {
        Payments payment = Payments.builder()
                .amount(amount)
                .paymentDate(new Date())
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .isDeposit(true)
                .status(PaymentStatus.PENDING)
                .appointments(appointment)
                .build();

        String transactionId = "BANK_MB_" + appointment.getId() + "_" + System.currentTimeMillis();
        payment.setTransactionId(transactionId);
        payment.setPaymentCode("COCLK" + appointment.getId());

        var qr = qrCodeService.getQRCode("BANK_TRANSFER", amount, appointment.getId());
        payment.setPaymentUrl(qr.getQrCodeUrl());

        return payment;
    }
}
```

---

### 5. MoMoPaymentStrategy

#### 5.1. Tạo DTO MoMo

```java
@Data
@Builder
public class MomoCreatePaymentRequest {
    private String partnerCode;
    private String accessKey;
    private String requestId;
    private long amount;
    private String orderId;
    private String orderInfo;
    private String returnUrl;
    private String notifyUrl;
    private String requestType; // "captureWallet"
    private String extraData;
    private String signature;
}

@Data
public class MomoCreatePaymentResponse {
    private int resultCode;
    private String message;
    private String payUrl;   // link thanh toán
    private String deeplink; // optional
    private String orderId;
}
```

#### 5.2. Client ký HMAC + gọi HTTP

```java
@Service
@RequiredArgsConstructor
public class MomoClient {

    @Value("${payment.momo.partner-code}")
    private String partnerCode;
    @Value("${payment.momo.access-key}")
    private String accessKey;
    @Value("${payment.momo.secret-key}")
    private String secretKey;
    @Value("${payment.momo.endpoint}")
    private String endpoint;
    @Value("${payment.momo.return-url}")
    private String returnUrl;
    @Value("${payment.momo.notify-url}")
    private String notifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public MomoCreatePaymentResponse createPayment(long amount, String orderId, String orderInfo) {
        String requestId = String.valueOf(System.currentTimeMillis());

        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&requestId=" + requestId
                + "&returnUrl=" + returnUrl
                + "&notifyUrl=" + notifyUrl
                + "&requestType=captureWallet";

        String signature = hmacSHA256(rawSignature, secretKey);

        MomoCreatePaymentRequest req = MomoCreatePaymentRequest.builder()
                .partnerCode(partnerCode)
                .accessKey(accessKey)
                .requestId(requestId)
                .amount(amount)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .returnUrl(returnUrl)
                .notifyUrl(notifyUrl)
                .requestType("captureWallet")
                .extraData("")
                .signature(signature)
                .build();

        return restTemplate.postForObject(endpoint, req, MomoCreatePaymentResponse.class);
    }

    private String hmacSHA256(String data, String key) {
        // TODO: implement using javax.crypto.Mac
        return "...";
    }
}
```

#### 5.3. Strategy MoMo

```java
@Service
@RequiredArgsConstructor
public class MomoPaymentStrategy implements PaymentGatewayStrategy {

    private final MomoClient momoClient;

    @Override
    public Payments createPayment(PaymentRequestDTO dto, User user, Appointments appointment, double amount) {
        String orderId = "APPT_" + appointment.getId() + "_" + System.currentTimeMillis();
        String orderInfo = "Thanh toan lich kham #" + appointment.getId();

        MomoCreatePaymentResponse momoRes = momoClient.createPayment((long) amount, orderId, orderInfo);

        if (momoRes == null || momoRes.getResultCode() != 0) {
            throw new IllegalStateException("Tạo thanh toán MoMo thất bại");
        }

        Payments payment = Payments.builder()
                .amount(amount)
                .paymentDate(new Date())
                .paymentMethod(PaymentMethod.MOMO)
                .isDeposit(true)
                .status(PaymentStatus.PENDING)
                .appointments(appointment)
                .transactionId(momoRes.getOrderId())
                .paymentCode("MOMO" + appointment.getId())
                .paymentUrl(momoRes.getPayUrl())
                .build();

        return payment;
    }
}
```

**Webhook MoMo**: tạo `/api/payments/momo-callback`, verify signature, tìm `Payments` theo `orderId`/`transactionId`, cập nhật `status` (`CONFIRMED` / `CANCELLED`) + update `Appointments.status` như bank-transfer.

---

### 6. VnpayPaymentStrategy

#### 6.1. Build URL thanh toán

```java
@Service
@RequiredArgsConstructor
public class VnpayClient {

    @Value("${payment.vnpay.tmn-code}")
    private String tmnCode;
    @Value("${payment.vnpay.hash-secret}")
    private String hashSecret;
    @Value("${payment.vnpay.endpoint}")
    private String endpoint;
    @Value("${payment.vnpay.return-url}")
    private String returnUrl;

    public String buildPaymentUrl(long amount, String orderId, String orderInfo, String ipAddr) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPAY dùng đơn vị là đồng * 100
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String query = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        String secureHash = hmacSHA512(hashSecret, query);

        return endpoint + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    private String hmacSHA512(String key, String data) {
        // TODO: implement using javax.crypto.Mac
        return "...";
    }
}
```

#### 6.2. Strategy VNPay

```java
@Service
@RequiredArgsConstructor
public class VnpayPaymentStrategy implements PaymentGatewayStrategy {

    private final VnpayClient vnpayClient;

    @Override
    public Payments createPayment(PaymentRequestDTO dto, User user, Appointments appointment, double amount) {
        String orderId = "APPT_" + appointment.getId() + "_" + System.currentTimeMillis();
        String orderInfo = "Thanh toan lich kham #" + appointment.getId();

        // TODO: lấy IP client thực tế, tạm thời để "127.0.0.1"
        String paymentUrl = vnpayClient.buildPaymentUrl((long) amount, orderId, orderInfo, "127.0.0.1");

        Payments payment = Payments.builder()
                .amount(amount)
                .paymentDate(new Date())
                .paymentMethod(PaymentMethod.VNPAY)
                .isDeposit(true)
                .status(PaymentStatus.PENDING)
                .appointments(appointment)
                .transactionId(orderId)
                .paymentCode("VNPAY" + appointment.getId())
                .paymentUrl(paymentUrl)
                .build();

        return payment;
    }
}
```

**Webhook VNPay**: tạo `/api/payments/vnpay-callback`:
- Đọc các tham số `vnp_TxnRef`, `vnp_ResponseCode`, `vnp_TransactionStatus`, `vnp_SecureHash`.  
- Verify hash với `hashSecret`.  
- Tìm `Payments` theo `transactionId` = `vnp_TxnRef`.  
- Nếu thành công (response code 00): set `PaymentStatus.CONFIRMED` + update `Appointments.status` như bank-transfer.

---

### 7. Factory chọn strategy & tích hợp vào `PaymentServiceIMPL`

```java
@Service
@RequiredArgsConstructor
public class PaymentGatewayFactory {

    private final BankTransferPaymentStrategy bankTransferPaymentStrategy;
    private final MomoPaymentStrategy momoPaymentStrategy;
    private final VnpayPaymentStrategy vnpayPaymentStrategy;

    public PaymentGatewayStrategy getStrategy(PaymentMethod method) {
        return switch (method) {
            case BANK_TRANSFER -> bankTransferPaymentStrategy;
            case MOMO -> momoPaymentStrategy;
            case VNPAY -> vnpayPaymentStrategy;
            case CASH -> null;
        };
    }
}
```

Trong `PaymentServiceIMPL.createPayment(...)`:

- Sau khi validate user + appointment + duplicate payment, thay đoạn tạo payment theo `paymentMethod` bằng:

```java
double amount = DEFAULT_DEPOSIT_AMOUNT;

Payments payment;

if (paymentMethod == PaymentMethod.CASH) {
    // giữ logic CASH hiện tại: tạo payment CONFIRMED + cập nhật appointment
} else {
    PaymentGatewayStrategy strategy = paymentGatewayFactory.getStrategy(paymentMethod);
    payment = strategy.createPayment(dto, user, appointment, amount);
    payment = paymentsRepository.save(payment);
}

PaymentResponseDTO response = paymentMapper.toDTO(payment);
response.setPaymentUrl(payment.getPaymentUrl());
return response;
```

Như vậy, để thêm/tắt MoMo/VNPay sau này bạn chỉ cần cấu hình strategy tương ứng mà không đụng vào `PaymentServiceIMPL`. 


