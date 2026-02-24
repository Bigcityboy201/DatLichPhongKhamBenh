package truonggg.payment.application;

import java.util.Map;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.PaymentResponseDTO;
import truonggg.dto.requestDTO.BankTransferCallbackDTO;
import truonggg.dto.requestDTO.PaymentRequestDTO;
import truonggg.reponse.PagedResult;

public interface PaymentService {

	// Tạo thanh toán (tạo payment record và trả về URL thanh toán nếu là online)
	PaymentResponseDTO createPayment(PaymentRequestDTO dto, String username);

	// Lấy danh sách thanh toán của user hiện tại

	PagedResult<PaymentResponseDTO> getMyPayments(String username, Pageable pageable);

	// Lấy danh sách tất cả thanh toán (ADMIN, EMPLOYEE)
	PagedResult<PaymentResponseDTO> getAllPayments(Pageable pageable);

	// Lấy chi tiết thanh toán theo ID
	PaymentResponseDTO getPaymentById(Integer paymentId, String username);

	// Lấy thanh toán theo appointment ID
	PagedResult<PaymentResponseDTO> getPaymentsByAppointment(Integer appointmentId, String username, Pageable pageable);

	// Kiểm tra trạng thái thanh toán
	PaymentResponseDTO checkPaymentStatus(Integer paymentId, String username);

	/*
	 * Xác nhận thanh toán chuyển khoản từ webhook (Google Apps Script/Casso) Tìm
	 * payment theo nội dung chuyển khoản và số tiền, sau đó cập nhật status
	 */
	PaymentResponseDTO confirmBankTransferPayment(BankTransferCallbackDTO callbackDTO);

	void processCassoWebhook(Map<String, Object> cassoData, String signatureHeader, String secretKeyHeader);
}


