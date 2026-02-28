package truonggg.payment.application.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import truonggg.Enum.Appointments_Enum;
import truonggg.Enum.PaymentMethod;
import truonggg.Enum.PaymentStatus;
import truonggg.Exception.NotFoundException;
import truonggg.appointment.domain.model.Appointments;
import truonggg.appointment.domain.service.AppointmentAccessValidator;
import truonggg.payment.domain.model.Payments;
import truonggg.user.domain.model.User;
import truonggg.dto.reponseDTO.PaymentResponseDTO;
import truonggg.dto.requestDTO.BankTransferCallbackDTO;
import truonggg.dto.requestDTO.PaymentRequestDTO;
import truonggg.payment.mapper.PaymentMapper;
import truonggg.appointment.infrastructure.AppointmentsRepository;
import truonggg.payment.infrastructure.PaymentsRepository;
import truonggg.user.infrastructure.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.payment.application.PaymentService;
import truonggg.strategy.PaymentStrategy;
import truonggg.strategy.impl.PaymentStrategyFactory;

@Service
@RequiredArgsConstructor
public class PaymentServiceIMPL implements PaymentService {

	// Số tiền cọc mặc định cho thanh toán chuyển khoản (QR)
	// private static final double DEFAULT_DEPOSIT_AMOUNT = 2000.0;

	@Value("${casso.webhook.secret-key}")
	private String cassoWebhookSecretKey;

	private final PaymentsRepository paymentsRepository;
	private final AppointmentsRepository appointmentsRepository;
	private final UserRepository userRepository;
	private final PaymentMapper paymentMapper;
    private final AppointmentAccessValidator appointmentAccessValidator;

	private final PaymentStrategyFactory paymentStrategyFactory;

	@Override
	@Transactional
	public PaymentResponseDTO createPayment(PaymentRequestDTO dto, String username) {

		// 1Kiểm tra user
		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User not found"));

		// 2Kiểm tra appointment
		Appointments appointment = appointmentsRepository.findById(dto.getAppointmentId())
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment not found"));

		// 3Kiểm tra đã thanh toán thành công chưa
		boolean hasSuccessPayment = paymentsRepository.existsByAppointmentsAndStatus(appointment,
				PaymentStatus.CONFIRMED);

        appointment.ensureCanBePaid();

		// 4Kiểm tra pending
		Optional<Payments> pendingPaymentOpt = paymentsRepository.findByAppointmentsAndStatus(appointment,
				PaymentStatus.PENDING);

		if (pendingPaymentOpt.isPresent()) {
			return paymentMapper.toDTO(pendingPaymentOpt.get());
		}

		// 5 Kiểm tra quyền
        this.appointmentAccessValidator.validatePermission(user, appointment);

		// 6 Kiểm tra appointment bị hủy
		if (appointment.getStatus() == Appointments_Enum.CANCELLED) {
			throw new IllegalArgumentException("Không thể thanh toán cho appointment đã bị hủy");
		}

		// 7 Resolve PaymentMethod
		PaymentMethod method = resolvePaymentMethod(dto.getPaymentMethod());

		// 8 Lấy Strategy tương ứng
		PaymentStrategy strategy = paymentStrategyFactory.getStrategy(method);

		// 9 Strategy xử lý payment
		Payments payment = strategy.processPayment(appointment, dto, user);

		// 10 Save DB
		payment = paymentsRepository.save(payment);

		// 11 Map DTO
		return paymentMapper.toDTO(payment);
	}

	@Override
	public PagedResult<PaymentResponseDTO> getMyPayments(String username, Pageable pageable) {
		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User not found"));

		Page<Payments> paymentsPage = paymentsRepository.findByAppointments_User_UserId(user.getUserId(), pageable);
		return convertToPagedResult(paymentsPage);
	}

	@Override
	public PagedResult<PaymentResponseDTO> getAllPayments(Pageable pageable) {
		Page<Payments> paymentsPage = paymentsRepository.findAll(pageable);
		return convertToPagedResult(paymentsPage);
	}

	@Override
	public PaymentResponseDTO getPaymentById(Integer paymentId, String username) {

		Payments payment = paymentsRepository.findById(paymentId)
				.orElseThrow(() -> new NotFoundException("payment", "Payment not found"));

		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User not found"));

		validatePaymentAccess(user, payment);

		return paymentMapper.toDTO(payment);
	}

	@Override
	public PagedResult<PaymentResponseDTO> getPaymentsByAppointment(Integer appointmentId, String username,
			Pageable pageable) {

		Appointments appointment = appointmentsRepository.findById(appointmentId)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment not found"));

		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User not found"));

		validateAppointmentAccess(user, appointment);

		Page<Payments> paymentsPage = paymentsRepository.findByAppointments_Id(appointmentId, pageable);

		return convertToPagedResult(paymentsPage);
	}

	@Override
	public PaymentResponseDTO checkPaymentStatus(Integer paymentId, String username) {
		return getPaymentById(paymentId, username);
	}

	@Override
	@Transactional
	public PaymentResponseDTO confirmBankTransferPayment(BankTransferCallbackDTO callbackDTO) {
		Payments payment = null;

		// 1)Khớp theo gatewayTransactionNo (tid từ bank)
		if (callbackDTO.getBankTransactionId() != null && !callbackDTO.getBankTransactionId().isEmpty()) {
			payment = paymentsRepository.findByGatewayTransactionNo(callbackDTO.getBankTransactionId()).orElse(null);
		}

		// 2) Nếu không tìm thấy, parse appointmentId từ content và tìm payment
		if (payment == null && callbackDTO.getContent() != null && !callbackDTO.getContent().isEmpty()) {
			Integer appointmentId = parseAppointmentIdFromContent(callbackDTO.getContent());

			if (appointmentId != null) {
				// Tìm payment theo appointmentId, amount, paymentMethod = BANK_TRANSFER, status
				// = PENDING
				List<Payments> pendingPayments = paymentsRepository
						.findByPaymentMethodAndAmountAndStatus(PaymentMethod.BANK_TRANSFER, callbackDTO.getAmount(),
								PaymentStatus.PENDING)
						.stream()
						.filter(p -> p.getAppointments() != null && p.getAppointments().getId().equals(appointmentId))
						.sorted((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate())) // Lấy payment mới nhất
						.toList();

				if (!pendingPayments.isEmpty()) {
					payment = pendingPayments.get(0);
				}
			}
		}

		if (payment == null) {
			return null;
		}

		//  Cập nhật trạng thái payment
		payment.setStatus(PaymentStatus.CONFIRMED);

		// Lưu gatewayTransactionNo nếu có
		if (callbackDTO.getBankTransactionId() != null && payment.getGatewayTransactionNo() == null) {
			payment.setGatewayTransactionNo(callbackDTO.getBankTransactionId());
		}

		// Lưu thông tin người chuyển nếu có
		if (callbackDTO.getFromAccount() != null) {
			String responseCodeValue = "STK: " + callbackDTO.getFromAccount();
			String payerName = payment.getAppointments() != null && payment.getAppointments().getUser() != null
					? payment.getAppointments().getUser().getFullName()
					: null;
			if (payerName == null || payerName.isBlank()) {
				payerName = callbackDTO.getFromName();
			}
			if (payerName != null && !payerName.isBlank()) {
				if (payerName.length() > 50) {
					payerName = payerName.substring(0, 50);
				}
				responseCodeValue += " | Tên: " + payerName;
			}
			if (responseCodeValue.length() > 100) {
				responseCodeValue = responseCodeValue.substring(0, 100);
			}
			payment.setResponseCode(responseCodeValue);
		}

		payment = paymentsRepository.saveAndFlush(payment);

		// Cập nhật appointment status nếu đang PENDING hoặc AWAITING_DEPOSIT
		Appointments appointment = payment.getAppointments();
		if (appointment != null && (appointment.getStatus() == Appointments_Enum.PENDING
				|| appointment.getStatus() == Appointments_Enum.AWAITING_DEPOSIT)) {
			appointment.setStatus(Appointments_Enum.CONFIRMED);
			appointmentsRepository.saveAndFlush(appointment);
		}

		return paymentMapper.toDTO(payment);
	}

	/**
	 * Parse appointmentId từ nội dung chuyển khoản Hỗ trợ format: "COC_LK_19",
	 * "COCLK19", "LK_19", "LK19", hoặc chỉ số
	 */
	private Integer parseAppointmentIdFromContent(String content) {
		if (content == null || content.isBlank())
			return null;

		// Remove all non-digit characters at the start, extract digits at the beginning
		String digits = "";
		for (char c : content.toCharArray()) {
			if (Character.isDigit(c)) {
				digits += c;
			} else if (!digits.isEmpty()) {
				break; // Dừng khi gặp ký tự đầu tiên không phải số sau khi đã bắt đầu lấy số
			}
		}

		if (digits.isEmpty())
			return null;

		try {
			return Integer.parseInt(digits);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private PagedResult<PaymentResponseDTO> convertToPagedResult(Page<Payments> paymentsPage) {
		List<PaymentResponseDTO> dtoList = paymentsPage.map(paymentMapper::toDTO).toList();
		return PagedResult.from(paymentsPage, dtoList);
	}

	@Transactional
	public void processCassoWebhook(Map<String, Object> cassoData, String signatureHeader, String secretKeyHeader) {

		verifyCassoWebhook(cassoData, signatureHeader, secretKeyHeader);

		Map<String, Object> dataToProcess = extractTransactionData(cassoData);

		String rawContent = firstNonNullString(dataToProcess.get("description"),
				dataToProcess.get("transaction_description"), dataToProcess.get("content"), dataToProcess.get("memo"),
				dataToProcess.get("remark"));

		String transactionTid = firstNonNullString(dataToProcess.get("transaction_tid"), dataToProcess.get("tid"));

		Double amount = parseAmount(dataToProcess.get("amount"), dataToProcess.get("value"), dataToProcess.get("money"),
				dataToProcess.get("transaction_amount"));

		if ((rawContent == null && transactionTid == null) || amount == null || amount <= 0) {
			return;
		}

		String normalizedContent = normalize(rawContent);

		BankTransferCallbackDTO callbackDTO = BankTransferCallbackDTO.builder().content(normalizedContent)
				.amount(amount).bankTransactionId(transactionTid).build();

		confirmBankTransferPayment(callbackDTO);
	}

	private Map<String, Object> extractTransactionData(Map<String, Object> cassoData) {

		if (cassoData.containsKey("transactions") && cassoData.get("transactions") instanceof List<?> list
				&& !list.isEmpty()) {
			return (Map<String, Object>) list.get(0);
		}

		if (cassoData.containsKey("data")) {
			Object dataObj = cassoData.get("data");

			if (dataObj instanceof Map) {
				return (Map<String, Object>) dataObj;
			}

			if (dataObj instanceof List<?> list && !list.isEmpty()) {
				return (Map<String, Object>) list.get(0);
			}
		}

		return cassoData;
	}

	// VERIFY WEBHOOK
	private void verifyCassoWebhook(Map<String, Object> cassoData, String signatureHeader, String secretKeyHeader) {

		if (secretKeyHeader != null && !secretKeyHeader.isEmpty()) {
			if (!cassoWebhookSecretKey.equals(secretKeyHeader)) {
				throw new SecurityException("Secret key không hợp lệ từ header");
			}
			return;
		}

		Object secretKeyFromBody = cassoData.get("secretKey");
		if (secretKeyFromBody != null) {
			if (!cassoWebhookSecretKey.equals(secretKeyFromBody.toString())) {
				throw new SecurityException("Secret key không hợp lệ từ body");
			}
		}
	}

	// ==============================
	// PARSE AMOUNT
	// ==============================
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

	private boolean isAdminOrEmployee(User user) {
		return user.getRole() != null && Boolean.TRUE.equals(user.getRole().getIsActive()) // role đang hoạt động
				&& ("ADMIN".equals(user.getRole().getRoleName()) || "EMPLOYEE".equals(user.getRole().getRoleName()));
	}

	private void validatePaymentAccess(User user, Payments payment) {
		if (!isAdminOrEmployee(user) && !payment.getAppointments().getUser().getUserId().equals(user.getUserId())) {
			throw new AccessDeniedException("Bạn không có quyền xem payment này");
		}
	}

	private void validateAppointmentAccess(User user, Appointments appointment) {
		if (!isAdminOrEmployee(user) && !appointment.getUser().getUserId().equals(user.getUserId())) {
			throw new AccessDeniedException("Bạn không có quyền xem payment của appointment này");
		}
	}

	private PaymentMethod resolvePaymentMethod(String method) {

		if (method == null || method.isBlank()) {
			return PaymentMethod.BANK_TRANSFER;
		}

		try {
			return PaymentMethod.valueOf(method.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ");
		}
	}

}


