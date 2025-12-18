package truonggg.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	@Value("${casso.webhook.secret-key}")
	private String cassoWebhookSecretKey;

	// Health check (GET/HEAD) to avoid 401 when Casso probes
	@GetMapping("/casso-webhook")
	public SuccessReponse<String> pingCassoWebhook() {
		return SuccessReponse.of("OK");
	}

	@RequestMapping(value = "/casso-webhook", method = RequestMethod.HEAD)
	public void pingCassoWebhookHead() {
		// Return 200 with empty body
	}

	@PostMapping
	@PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'EMPLOYEE')")
	public SuccessReponse<PaymentResponseDTO> createPayment(@RequestBody @Valid PaymentRequestDTO dto) {
		// Lấy username của người đang đăng nhập
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		// Gọi service tạo payment
		PaymentResponseDTO payment = paymentService.createPayment(dto, username);

		return SuccessReponse.of(payment);
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

	@PostMapping("/casso-webhook")
	public SuccessReponse<String> handleCassoWebhook(@RequestBody Map<String, Object> cassoData,
			@RequestHeader(value = "X-Casso-Signature", required = false) String signatureHeader,
			@RequestHeader(value = "X-Secret-Key", required = false) String secretKeyHeader) {

		try {
			verifyCassoWebhook(cassoData, signatureHeader, secretKeyHeader);

			Map<String, Object> dataToProcess = cassoData;

			if (cassoData.containsKey("data")) {
				Object dataObj = cassoData.get("data");
				if (dataObj instanceof Map) {
					dataToProcess = (Map<String, Object>) dataObj;
				} else if (dataObj instanceof List<?> list && !list.isEmpty()) {
					dataToProcess = (Map<String, Object>) list.get(0);
				}
			}

			if (cassoData.containsKey("transactions") && cassoData.get("transactions") instanceof List<?> list
					&& !list.isEmpty()) {
				dataToProcess = (Map<String, Object>) list.get(0);
			}

			String rawContent = firstNonNullString(dataToProcess.get("description"),
					dataToProcess.get("transaction_description"), dataToProcess.get("content"),
					dataToProcess.get("memo"), dataToProcess.get("remark"));

			String transactionTid = firstNonNullString(dataToProcess.get("transaction_tid"), dataToProcess.get("tid"));

			Double amount = parseAmount(dataToProcess.get("amount"), dataToProcess.get("value"),
					dataToProcess.get("money"), dataToProcess.get("transaction_amount"));

			if ((rawContent == null && transactionTid == null) || amount == null || amount <= 0) {
				return SuccessReponse.of("IGNORED");
			}

			// Normalize description
			String normalizedContent = rawContent == null ? null : normalize(rawContent);
			if (normalizedContent != null && normalizedContent.contains("-")) {
				normalizedContent = normalizedContent.split("-")[0].trim();
			}

			// Tạo DTO gửi service, bao gồm tid
			BankTransferCallbackDTO callbackDTO = BankTransferCallbackDTO.builder().content(normalizedContent)
					.amount(amount).bankTransactionId(transactionTid).build();

			PaymentResponseDTO result = paymentService.confirmBankTransferPayment(callbackDTO);

		} catch (Exception e) {
			// Ignore errors to avoid breaking webhook flow
		}

		return SuccessReponse.of("RECEIVED");
	}

	private Double parseAmount(Object... values) {
		for (Object v : values) {
			if (v == null)
				continue;

			try {
				if (v instanceof Number) {
					return ((Number) v).doubleValue();
				}
				return Double.parseDouble(v.toString());
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	private void verifyCassoWebhook(Map<String, Object> cassoData, String signatureHeader, String secretKeyHeader) {
		// Kiểm tra secret key từ header (Casso có thể gửi trong header)
		if (secretKeyHeader != null && !secretKeyHeader.isEmpty()) {
			if (!cassoWebhookSecretKey.equals(secretKeyHeader)) {
				throw new SecurityException("Secret key không hợp lệ từ header");
			}
			return;
		}

		// Kiểm tra secret key từ body (Casso có thể gửi trong body)
		Object secretKeyFromBody = cassoData.get("secretKey");
		if (secretKeyFromBody != null) {
			String secretKeyStr = secretKeyFromBody.toString();
			if (!cassoWebhookSecretKey.equals(secretKeyStr)) {
				throw new SecurityException("Secret key không hợp lệ từ body");
			}
			return;
		}

		// Kiểm tra signature nếu có (Casso có thể dùng signature để verify)
		if (signatureHeader != null && !signatureHeader.isEmpty()) {
			// Có thể implement signature verification nếu Casso hỗ trợ
		}

		// Nếu không có secret key nào được gửi, có thể cho phép hoặc từ chối
		// Tùy vào yêu cầu bảo mật, bạn có thể uncomment dòng sau để bắt buộc phải có
		// secret key
		// throw new SecurityException("Không tìm thấy secret key trong request");
	}

	/**
	 * Helper: lấy giá trị String đầu tiên không null/blank trong danh sách
	 */
	private String firstNonNullString(Object... values) {
		for (Object v : values) {
			if (v != null) {
				String s = v.toString().trim();
				if (!s.isEmpty()) {
					return s;
				}
			}
		}
		return null;
	}

	private String normalize(String s) {
		return s == null ? null : s.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
	}
}
