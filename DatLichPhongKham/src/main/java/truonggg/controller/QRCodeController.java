package truonggg.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.QRCodeResponseDTO;
import truonggg.reponse.SuccessReponse;
import truonggg.service.QRCodeService;

@RestController
@RequestMapping(path = "/api/qrcode")
@RequiredArgsConstructor
public class QRCodeController {
	
	private final QRCodeService qrCodeService;
	
	/**
	 * GET /api/qrcode/deposit - Lấy QR code cho đặt cọc mặc định 2000 đồng
	 * @param paymentMethod TIMO hoặc BANK_TRANSFER (cho Timo), MOMO (không hỗ trợ QR tĩnh)
	 * @param appointmentId ID của appointment (optional, để tạo nội dung chuyển khoản)
	 * @return QRCodeResponseDTO
	 */
	@GetMapping("/deposit")
	@PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'EMPLOYEE')")
	public SuccessReponse<QRCodeResponseDTO> getDepositQRCode(
			@RequestParam(value = "paymentMethod", defaultValue = "TIMO") String paymentMethod,
			@RequestParam(value = "appointmentId", required = false) Integer appointmentId) {
		QRCodeResponseDTO qrCode = qrCodeService.getDepositQRCode(paymentMethod, appointmentId);
		return SuccessReponse.of(qrCode);
	}
	
	/**
	 * GET /api/qrcode - Lấy QR code với số tiền tùy chỉnh
	 * @param paymentMethod TIMO hoặc BANK_TRANSFER
	 * @param amount Số tiền (mặc định 2000)
	 * @param appointmentId ID của appointment (optional)
	 * @return QRCodeResponseDTO
	 */
	@GetMapping
	@PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'EMPLOYEE')")
	public SuccessReponse<QRCodeResponseDTO> getQRCode(
			@RequestParam(value = "paymentMethod", defaultValue = "TIMO") String paymentMethod,
			@RequestParam(value = "amount", defaultValue = "2000") Double amount,
			@RequestParam(value = "appointmentId", required = false) Integer appointmentId) {
		QRCodeResponseDTO qrCode = qrCodeService.getQRCode(paymentMethod, amount, appointmentId);
		return SuccessReponse.of(qrCode);
	}
}

