package truonggg.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.QRCodeResponseDTO;
import truonggg.service.QRCodeService;

@Service
@RequiredArgsConstructor
public class QRCodeServiceIMPL implements QRCodeService {

	@Value("${qrcode.bank.bank-id:970422}")
	private String bankId;

	@Value("${qrcode.bank.account:}")
	private String bankAccount;

	@Value("${qrcode.bank.template:compact2}")
	private String bankTemplate;

	@Value("${qrcode.bank.name:MB Bank}")
	private String bankName;

	@Override
	public QRCodeResponseDTO generateQRCode(Double amount, Integer appointmentId) {

		// 1)Kiểm tra cấu hình ngân hàng
		if (bankAccount == null || bankAccount.isEmpty()) {
			throw new IllegalStateException("Chưa cấu hình tài khoản ngân hàng");
		}

		// 2️)Tạo nội dung chuyển khoản
		String content = generatePaymentContent(appointmentId);

		// 3️) Encode nội dung
		String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);

		// 4️) Tạo URL VietQR
		String qrCodeUrl = String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%.0f&addInfo=%s", bankId,
				bankAccount, bankTemplate, amount, encodedContent);

		// 5️) Trả DTO
		return QRCodeResponseDTO.builder().qrCodeUrl(qrCodeUrl).amount(amount).paymentMethod("BANK_TRANSFER")
				.accountNumber(bankAccount).bankName(bankName).content(content).template(bankTemplate).build();
	}

	private String generatePaymentContent(Integer appointmentId) {
		return "COCLK" + appointmentId;
	}
}
