package truonggg.controller;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.PaymentResponseDTO;
import truonggg.dto.requestDTO.BankTransferCallbackDTO;
import truonggg.dto.requestDTO.PaymentRequestDTO;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.service.PaymentService;

@RestController
@RequestMapping(path = "/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	@PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'EMPLOYEE')")
	public SuccessReponse<PaymentResponseDTO> createPayment(@RequestBody @Valid PaymentRequestDTO dto) {
		// Lấy username của người đang đăng nhập
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		// Gọi service tạo payment
		PaymentResponseDTO payment = paymentService.createPayment(dto, username);

		return SuccessReponse.of(payment);
	}

	// POST /api/payments/momo-callback - Callback từ MoMo

	@PostMapping("/momo-callback")
	public SuccessReponse<PaymentResponseDTO> handleMomoCallback(@RequestBody Map<String, Object> callbackParams) {
		// Convert Map<String, Object> sang Map<String, String>
		Map<String, String> stringParams = new java.util.HashMap<>();
		for (Map.Entry<String, Object> entry : callbackParams.entrySet()) {
			if (entry.getValue() != null) {
				stringParams.put(entry.getKey(), entry.getValue().toString());
			}
		}
		return SuccessReponse.of(paymentService.handleMomoCallback(stringParams));
	}

	// GET /api/payments/me - Lấy danh sách thanh toán của user hiện tại

	@GetMapping("/me")
	@PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'EMPLOYEE')")
	public SuccessReponse<?> getMyPayments(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<PaymentResponseDTO> pagedResult = paymentService.getMyPayments(username, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/payments - Lấy tất cả thanh toán (ADMIN, EMPLOYEE)
	@GetMapping
	@PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
	public SuccessReponse<?> getAllPayments(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<PaymentResponseDTO> pagedResult = paymentService.getAllPayments(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/payments/{id} - Lấy chi tiết thanh toán

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'EMPLOYEE')")
	public SuccessReponse<PaymentResponseDTO> getPaymentById(@PathVariable Integer id) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(paymentService.getPaymentById(id, username));
	}

	// GET /api/payments/appointment/{appointmentId} - Lấy thanh toán theo
	// appointment

	@GetMapping("/appointment/{appointmentId}")
	@PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'EMPLOYEE')")
	public SuccessReponse<?> getPaymentsByAppointment(@PathVariable Integer appointmentId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<PaymentResponseDTO> pagedResult = paymentService.getPaymentsByAppointment(appointmentId, username,
				pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/payments/{id}/status - Kiểm tra trạng thái thanh toán
	@GetMapping("/{id}/status")
	@PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'EMPLOYEE')")
	public SuccessReponse<PaymentResponseDTO> checkPaymentStatus(@PathVariable Integer id) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(paymentService.checkPaymentStatus(id, username));
	}

	@PostMapping("/bank-transfer-callback")
	public SuccessReponse<PaymentResponseDTO> handleBankTransferCallback(
			@RequestBody BankTransferCallbackDTO callbackDTO) {
		// Không cần authentication vì đây là webhook từ external service
		return SuccessReponse.of(paymentService.confirmBankTransferPayment(callbackDTO));
	}

//	@PostMapping("/casso-webhook")
//	public SuccessReponse<PaymentResponseDTO> handleCassoWebhook(@RequestBody Map<String, Object> cassoData) {
//		// Không cần authentication vì đây là webhook từ external service
//		try {
//			// Parse dữ liệu từ Casso
//			String description = cassoData.get("description") != null ? cassoData.get("description").toString() : null;
//
//			Double amount = null;
//			if (cassoData.get("amount") != null) {
//				Object amountObj = cassoData.get("amount");
//				if (amountObj instanceof Number) {
//					amount = ((Number) amountObj).doubleValue();
//				} else {
//					amount = Double.parseDouble(amountObj.toString());
//				}
//			}
//
//			String subAccId = cassoData.get("subAccId") != null ? cassoData.get("subAccId").toString() : null;
//
//			String tid = cassoData.get("tid") != null ? cassoData.get("tid").toString() : null;
//
//			String when = cassoData.get("when") != null ? cassoData.get("when").toString() : null;
//
//			// Chuyển đổi sang BankTransferCallbackDTO
//			BankTransferCallbackDTO callbackDTO = BankTransferCallbackDTO.builder().content(description).amount(amount)
//					.fromAccount(subAccId).fromName(subAccId) // Casso có thể không có tên riêng, dùng STK
//					.transactionDate(when).bankTransactionId(tid).build();
//
//			// Xử lý payment
//			return SuccessReponse.of(paymentService.confirmBankTransferPayment(callbackDTO));
//
//		} catch (Exception e) {
//			throw new IllegalArgumentException("Lỗi xử lý webhook từ Casso: " + e.getMessage());
//		}
//	}
}
