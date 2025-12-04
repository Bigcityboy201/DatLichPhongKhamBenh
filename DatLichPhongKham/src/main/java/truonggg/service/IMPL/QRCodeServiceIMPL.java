package truonggg.service.IMPL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.QRCodeResponseDTO;
import truonggg.Model.Appointments;
import truonggg.repo.AppointmentsRepository;
import truonggg.service.QRCodeService;

@Service
@RequiredArgsConstructor
public class QRCodeServiceIMPL implements QRCodeService {
	
	private final AppointmentsRepository appointmentsRepository;
	
	// Timo Bank Configuration
	@Value("${qrcode.timo.bank-id:970415}")
	private String timoBankId;
	
	@Value("${qrcode.timo.account:}")
	private String timoAccount;
	
	@Value("${qrcode.timo.template:compact2}")
	private String timoTemplate;
	
	// Default deposit amount
	private static final Double DEFAULT_DEPOSIT_AMOUNT = 2000.0;
	
	@Override
	public QRCodeResponseDTO getDepositQRCode(String paymentMethod, Integer appointmentId) {
		return getQRCode(paymentMethod, DEFAULT_DEPOSIT_AMOUNT, appointmentId);
	}
	
	@Override
	public QRCodeResponseDTO getQRCode(String paymentMethod, Double amount, Integer appointmentId) {
		String method = paymentMethod.toUpperCase();
		
		if ("TIMO".equals(method) || "BANK_TRANSFER".equals(method)) {
			return generateTimoQRCode(amount, appointmentId);
		} else if ("MOMO".equals(method)) {
			// MoMo không dùng VietQR, nên trả về thông báo hoặc redirect đến MoMo payment
			throw new IllegalArgumentException("MoMo không hỗ trợ QR code tĩnh. Vui lòng sử dụng API thanh toán MoMo.");
		} else {
			throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ. Chỉ chấp nhận: TIMO, BANK_TRANSFER, MOMO");
		}
	}
	
	private QRCodeResponseDTO generateTimoQRCode(Double amount, Integer appointmentId) {
		// Validate tài khoản Timo
		if (timoAccount == null || timoAccount.isEmpty()) {
			throw new IllegalStateException("Chưa cấu hình tài khoản Timo. Vui lòng cấu hình trong application.properties");
		}
		
		// Tạo nội dung chuyển khoản
		String content = generatePaymentContent(appointmentId);
		
		// Tạo URL QR code từ VietQR API
		// Format: https://img.vietqr.io/image/{BANK_ID}-{ACCOUNT}-{TEMPLATE}.png?amount={AMOUNT}&addInfo={CONTENT}
		String qrCodeUrl = String.format(
			"https://img.vietqr.io/image/%s-%s-%s.png?amount=%.0f&addInfo=%s",
			timoBankId,
			timoAccount,
			timoTemplate,
			amount,
			content
		);
		
		return QRCodeResponseDTO.builder()
				.qrCodeUrl(qrCodeUrl)
				.amount(amount)
				.paymentMethod("TIMO")
				.accountNumber(timoAccount)
				.bankName("Timo")
				.content(content)
				.template(timoTemplate)
				.build();
	}
	
	private String generatePaymentContent(Integer appointmentId) {
		if (appointmentId != null) {
			// Kiểm tra appointment có tồn tại không
			Appointments appointment = appointmentsRepository.findById(appointmentId)
					.orElse(null); // Không throw exception để có thể tạo QR code trước khi có appointment
			
			if (appointment != null) {
				return String.format("COC_LK_%d", appointmentId);
			}
		}
		// Nếu không có appointmentId, dùng mã mặc định
		return "COC_PHONGKHAM";
	}
}

