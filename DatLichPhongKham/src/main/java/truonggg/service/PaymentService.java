package truonggg.service;

import java.util.Map;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.PaymentResponseDTO;
import truonggg.dto.requestDTO.PaymentRequestDTO;
import truonggg.reponse.PagedResult;

public interface PaymentService {
	
	/**
	 * Tạo thanh toán (tạo payment record và trả về URL thanh toán nếu là online)
	 */
	PaymentResponseDTO createPayment(PaymentRequestDTO dto, String username);
	
	/**
	 * Xử lý callback từ MoMo
	 */
	PaymentResponseDTO handleMomoCallback(Map<String, String> callbackParams);
	
	/**
	 * Lấy danh sách thanh toán của user hiện tại
	 */
	PagedResult<PaymentResponseDTO> getMyPayments(String username, Pageable pageable);
	
	/**
	 * Lấy danh sách tất cả thanh toán (ADMIN, EMPLOYEE)
	 */
	PagedResult<PaymentResponseDTO> getAllPayments(Pageable pageable);
	
	/**
	 * Lấy chi tiết thanh toán theo ID
	 */
	PaymentResponseDTO getPaymentById(Integer paymentId, String username);
	
	/**
	 * Lấy thanh toán theo appointment ID
	 */
	PagedResult<PaymentResponseDTO> getPaymentsByAppointment(Integer appointmentId, String username, Pageable pageable);
	
	/**
	 * Kiểm tra trạng thái thanh toán
	 */
	PaymentResponseDTO checkPaymentStatus(Integer paymentId, String username);
}

