package truonggg.service;

import truonggg.dto.reponseDTO.QRCodeResponseDTO;

public interface QRCodeService {

	QRCodeResponseDTO getDepositQRCode(String paymentMethod, Integer appointmentId);

	QRCodeResponseDTO getQRCode(String paymentMethod, Double amount, Integer appointmentId);
}
