package truonggg.service;

import truonggg.dto.reponseDTO.QRCodeResponseDTO;

public interface QRCodeService {

	QRCodeResponseDTO generateQRCode(Double amount, Integer appointmentId);
}
