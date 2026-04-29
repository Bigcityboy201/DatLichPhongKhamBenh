package truonggg.payment.application;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import truonggg.payment.domain.model.Payments;

/**
 * Service tạo URL thanh toán VNPAY.
 *
 * Các cấu hình sẽ được bạn điền trong application-*.yml:
 *
 * vnpay:
 *   tmn-code: YOUR_TMN_CODE
 *   hash-secret: YOUR_HASH_SECRET
 *   pay-url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
 *   return-url: https://your-frontend-domain.com/vnpay/return
 */
@Service
public class VnPayService {

    @Value("${vnpay.tmn-code:}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret:}")
    private String vnpHashSecret;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url:http://localhost:3000/vnpay-return}")
    private String vnpReturnUrl;

    // Locale & default config
    private static final String VNP_VERSION = "2.1.0";
    private static final String VNP_COMMAND = "pay";
    private static final String VNP_CURRENCY = "VND";
    // Mã loại hàng hóa, theo doc VNPAY (ví dụ: other, billpayment, fashion,...)
    private static final String VNP_ORDER_TYPE = "other";

    @PostConstruct
    public void validateConfig() {
        // Không throw để dev có thể chạy, nhưng log cảnh báo nếu thiếu cấu hình
        if (vnpTmnCode == null || vnpTmnCode.isBlank()
                || vnpHashSecret == null || vnpHashSecret.isBlank()) {
            // Ở môi trường thật bạn nên log cảnh báo / throw exception
        }
    }

    /**
     * Tạo URL thanh toán VNPAY cho một payment.
     * VNPAY yêu cầu amount * 100 (VND -> số nguyên).
     */
    public String createPaymentUrl(Payments payment) {
        // Thời gian tạo & hết hạn (VD: +15 phút)
        TimeZone tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(tz);

        String createDate = formatter.format(new Date());
        String expireDate = formatter.format(new Date(System.currentTimeMillis() + 15 * 60 * 1000));

        // Số tiền theo format VNPAY: nhân 100 (VND -> số nguyên)
        long amountVnp = Math.round(payment.getAmount() * 100);

        String orderId = payment.getTransactionId(); // Đã được set trong Strategy
        String orderInfo = "Thanh_toan_dat_lich_" + payment.getPaymentCode();

        SortedMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", VNP_VERSION);
        params.put("vnp_Command", VNP_COMMAND);
        params.put("vnp_TmnCode", vnpTmnCode);
        params.put("vnp_Amount", String.valueOf(amountVnp));
        params.put("vnp_CurrCode", VNP_CURRENCY);
        params.put("vnp_OrderType", VNP_ORDER_TYPE);
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnpReturnUrl);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);
        // IP client: tạm thời để 127.0.0.1, bạn có thể sửa để truyền IP thực từ FE
        params.put("vnp_IpAddr", "127.0.0.1");

        String queryString = buildQueryString(params);
        String hashData = buildHashData(params);
        String secureHash = hmacSHA512(vnpHashSecret, hashData);

        String paymentUrl = vnpPayUrl + "?" + queryString + "&vnp_SecureHash=" + secureHash;

        System.out.println("HASH DATA: " + hashData);
        System.out.println("QUERY STRING: " + queryString);
        System.out.println("PAYMENT URL: " + paymentUrl);

        return paymentUrl;
    }

    private String buildQueryString(SortedMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (var entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) continue;
            if (sb.length() > 0) sb.append('&');
            sb.append(urlEncode(entry.getKey()))
              .append('=')
              .append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    private String buildHashData(SortedMap<String, String> params) {
        // Theo sample VNPAY: hashData là chuỗi key=value nối bằng &,
        // value được URL-encode giống query string.
        StringBuilder sb = new StringBuilder();
        for (var entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) continue;
            if (sb.length() > 0) sb.append('&');
            sb.append(entry.getKey())
              .append('=')
              .append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    private String urlEncode(String value) {
        try {
            // VNPAY sample dùng US-ASCII
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hash.append('0');
                hash.append(hex);
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating VNPAY HMAC-SHA512", e);
        }
    }
}


