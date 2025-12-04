package truonggg.service;

import truonggg.dto.reponseDTO.QRCodeResponseDTO;

public interface QRCodeService {
	
	/**
	 * Lấy QR code cho đặt cọc với số tiền mặc định 2000 đồng
	 * @param paymentMethod TIMO hoặc MOMO
	 * @param appointmentId ID của appointment (để tạo nội dung chuyển khoản)
	 * @return QRCodeResponseDTO chứa URL QR code và thông tin thanh toán
	 */
	QRCodeResponseDTO getDepositQRCode(String paymentMethod, Integer appointmentId);
	
	/**
	 * Lấy QR code với số tiền tùy chỉnh
	 * @param paymentMethod TIMO hoặc MOMO
	 * @param amount Số tiền
	 * @param appointmentId ID của appointment
	 * @return QRCodeResponseDTO
	 */
	QRCodeResponseDTO getQRCode(String paymentMethod, Double amount, Integer appointmentId);
}

