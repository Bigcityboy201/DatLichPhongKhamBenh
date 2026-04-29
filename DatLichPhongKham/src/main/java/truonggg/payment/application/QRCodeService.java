package truonggg.payment.application;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.QRCodeResponseDTO;
import truonggg.payment.domain.model.Payments;

/**
 * Service tạo URL QR thanh toán ngân hàng (VietQR) cho thanh toán chuyển khoản.
 *
 * Cấu hình được lấy từ application-*.yml với prefix qrcode.bank.*
 * Ví dụ (application-dev.yml):
 *
 * qrcode:
 *   bank:
 *     bank-id: 970422
 *     account: 0363159912
 *     template: compact2
 *     name: MB Bank
 */
@Service
@RequiredArgsConstructor
public class QRCodeService {

    private static final String VIETQR_BASE_URL = "https://img.vietqr.io/image";

    @Value("${qrcode.bank.bank-id}")
    private String bankId;

    @Value("${qrcode.bank.account}")
    private String accountNumber;

    @Value("${qrcode.bank.template:compact2}")
    private String template;

    @Value("${qrcode.bank.name:MB Bank}")
    private String bankName;

    /**
     * Tạo QR code VietQR cho thanh toán chuyển khoản.
     *
     * - amount: số tiền cần thanh toán (đồng)
     * - content: nội dung chuyển khoản để đối soát (thường dùng paymentCode hoặc COCLK + appointmentId)
     *
     * URL format tham khảo:
     * https://img.vietqr.io/image/{bankId}-{account}-{template}.jpg?amount={amount}&addInfo={content}&accountName={accountName}
     */
    public QRCodeResponseDTO generateForPayment(Payments payment) {
        double amount = payment.getAmount();
        // Nội dung chuyển khoản ưu tiên dùng paymentCode nếu có, fallback sang transactionId
        String content = payment.getPaymentCode() != null
                ? payment.getPaymentCode()
                : payment.getTransactionId();

        String encodedContent = urlEncode(content);
        String encodedAccountName = urlEncode(bankName);

        long roundedAmount = Math.round(amount);

        String qrCodeUrl = String.format(
                "%s/%s-%s-%s.jpg?amount=%d&addInfo=%s&accountName=%s",
                VIETQR_BASE_URL,
                bankId,
                accountNumber,
                template,
                roundedAmount,
                encodedContent,
                encodedAccountName
        );

        return QRCodeResponseDTO.builder()
                .qrCodeUrl(qrCodeUrl)
                .amount(amount)
                .paymentMethod(payment.getPaymentMethod().name())
                .accountNumber(accountNumber)
                .bankName(bankName)
                .content(content)
                .template(template)
                .build();
    }

    private String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}


