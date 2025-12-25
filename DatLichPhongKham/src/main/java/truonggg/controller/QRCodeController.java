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

	@GetMapping("/deposit")
	@PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'EMPLOYEE')")
	public SuccessReponse<QRCodeResponseDTO> getDepositQRCode(
			@RequestParam(value = "paymentMethod", defaultValue = "BANK_TRANSFER") String paymentMethod,
			@RequestParam(value = "appointmentId", required = false) Integer appointmentId) {
		QRCodeResponseDTO qrCode = qrCodeService.getDepositQRCode(paymentMethod, appointmentId);
		return SuccessReponse.of(qrCode);
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'EMPLOYEE')")
	public SuccessReponse<QRCodeResponseDTO> getQRCode(
			@RequestParam(value = "paymentMethod", defaultValue = "BANK_TRANSFER") String paymentMethod,
			@RequestParam(value = "amount", defaultValue = "2000") Double amount,
			@RequestParam(value = "appointmentId", required = false) Integer appointmentId) {
		QRCodeResponseDTO qrCode = qrCodeService.getQRCode(paymentMethod, amount, appointmentId);
		return SuccessReponse.of(qrCode);
	}
}
