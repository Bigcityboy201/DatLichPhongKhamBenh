package truonggg.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.QRCodeResponseDTO;
import truonggg.repo.AppointmentsRepository;
import truonggg.service.QRCodeService;

@Service
@RequiredArgsConstructor
public class QRCodeServiceIMPL implements QRCodeService {

	private final AppointmentsRepository appointmentsRepository;

	// Bank Configuration (MB Bank)
	@Value("${qrcode.bank.bank-id:970422}")
	private String bankId; // MB Bank: 970422

	@Value("${qrcode.bank.account:}")
	private String bankAccount;

	@Value("${qrcode.bank.template:compact2}")
	private String bankTemplate;

	@Value("${qrcode.bank.name:MB Bank}")
	private String bankName;

	// Default deposit amount
	private static final Double DEFAULT_DEPOSIT_AMOUNT = 2000.0;

	@Override
	public QRCodeResponseDTO getDepositQRCode(String paymentMethod, Integer appointmentId) {
		return getQRCode(paymentMethod, DEFAULT_DEPOSIT_AMOUNT, appointmentId);
	}

	@Override
	public QRCodeResponseDTO getQRCode(String paymentMethod, Double amount, Integer appointmentId) {
		String method = paymentMethod.toUpperCase();

		if ("BANK_TRANSFER".equals(method) || "MB".equals(method)) {
			return generateBankQRCode(amount, appointmentId);
		} else {
			throw new IllegalArgumentException(
					"Phương thức thanh toán không hợp lệ. Chỉ chấp nhận: BANK_TRANSFER, MB");
		}
	}

	private QRCodeResponseDTO generateBankQRCode(Double amount, Integer appointmentId) {

		if (bankAccount == null || bankAccount.isEmpty()) {
			throw new IllegalStateException("Chưa cấu hình tài khoản ngân hàng");
		}

		// ✅ Nội dung chuyển khoản chuẩn Casso
		String content = generatePaymentContent(appointmentId);

		// ✅ BẮT BUỘC encode addInfo
		String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);

		// VietQR URL
		String qrCodeUrl = String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%.0f&addInfo=%s", bankId,
				bankAccount, bankTemplate, amount, encodedContent);

		return QRCodeResponseDTO.builder().qrCodeUrl(qrCodeUrl).amount(amount).paymentMethod("BANK_TRANSFER")
				.accountNumber(bankAccount).bankName(bankName).content(content).template(bankTemplate).build();
	}

	/**
	 * Nội dung chuyển khoản: VD: COCLK21
	 */
	private String generatePaymentContent(Integer appointmentId) {
		if (appointmentId != null) {
			return "COCLK" + appointmentId;
		}
		return "COCPHONGKHAM";
	}
}
