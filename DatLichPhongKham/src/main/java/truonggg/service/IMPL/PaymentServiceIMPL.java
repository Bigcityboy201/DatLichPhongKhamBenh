package truonggg.service.IMPL;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import truonggg.Enum.Appointments_Enum;
import truonggg.Enum.PaymentMethod;
import truonggg.Exception.NotFoundException;
import truonggg.Model.Appointments;
import truonggg.Model.Payments;
import truonggg.Model.User;
import truonggg.dto.reponseDTO.PaymentResponseDTO;
import truonggg.dto.requestDTO.BankTransferCallbackDTO;
import truonggg.dto.requestDTO.PaymentRequestDTO;
import truonggg.mapper.PaymentMapper;
import truonggg.repo.AppointmentsRepository;
import truonggg.repo.PaymentsRepository;
import truonggg.repo.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.PaymentService;
import truonggg.service.QRCodeService;
import truonggg.utils.MomoUtils;

@Service
@RequiredArgsConstructor
public class PaymentServiceIMPL implements PaymentService {

	// Số tiền cọc mặc định cho thanh toán chuyển khoản (QR)
	private static final double DEFAULT_DEPOSIT_AMOUNT = 2000.0;

	private final PaymentsRepository paymentsRepository;
	private final AppointmentsRepository appointmentsRepository;
	private final UserRepository userRepository;
	private final PaymentMapper paymentMapper;
	private final MomoUtils momoUtils;
	private final ObjectMapper objectMapper;
	private final QRCodeService qrCodeService;

	@Override
	@Transactional
	public PaymentResponseDTO createPayment(PaymentRequestDTO dto, String username) {

		// Lấy user đang thao tác
		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User not found"));

		// Lấy appointment
		Appointments appointment = appointmentsRepository.findById(dto.getAppointmentId())
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment not found"));

		// Kiểm tra quyền
		boolean isAdminOrEmployee = user.getRole() != null && !user.getRole().getIsActive()
				&& (user.getRole().getRoleName().equals("ADMIN") || user.getRole().getRoleName().equals("EMPLOYEE"));

		if (!isAdminOrEmployee && !appointment.getUser().getUserId().equals(user.getUserId())) {
			throw new AccessDeniedException("Bạn không có quyền thanh toán cho appointment này");
		}

		// Kiểm tra trạng thái appointment
		if (appointment.getStatus() == Appointments_Enum.CANCELLED) {
			throw new IllegalArgumentException("Không thể thanh toán cho appointment đã bị hủy");
		}

		// Validate payment method
		PaymentMethod paymentMethod;
		try {
			paymentMethod = PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"Phương thức thanh toán không hợp lệ. Chỉ chấp nhận: MOMO, CASH, BANK_TRANSFER");
		}

		double amount;

		switch (paymentMethod) {
		case BANK_TRANSFER -> amount = DEFAULT_DEPOSIT_AMOUNT; // ví dụ 2000
		case MOMO -> amount = DEFAULT_DEPOSIT_AMOUNT; // cũng dùng mức cọc mặc định
		case CASH -> amount = DEFAULT_DEPOSIT_AMOUNT; // hoặc set theo quy tắc riêng
		default -> throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ");
		}

		if (amount <= 0) {
			throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
		}
		// KẾT THÚC xử lý amount

		// Tạo payment record
		Payments payment = Payments.builder().amount(amount).paymentDate(new Date()).paymentMethod(paymentMethod)
				.isDeposit(true).status(Appointments_Enum.PENDING).appointments(appointment).build();

		String paymentUrl = null;
		String transactionId = null;

		// Xử lý theo phương thức
		switch (paymentMethod) {
		case MOMO -> {
			transactionId = momoUtils.generateOrderId(dto.getAppointmentId());
			payment.setTransactionId(transactionId);

			Long amountLong = (long) (amount * 100);
			String orderInfo = "Thanh toan dat coc cho lich hen #" + dto.getAppointmentId();
			Map<String, String> paymentRequest = momoUtils.createPaymentRequest(transactionId, amountLong, orderInfo);
			paymentUrl = convertMapToJsonString(paymentRequest);
			payment.setPaymentUrl(paymentUrl);
		}

		case BANK_TRANSFER -> {
			transactionId = "BANK_" + dto.getAppointmentId() + "_" + System.currentTimeMillis();
			payment.setTransactionId(transactionId);

			try {
				var qrCodeResponse = qrCodeService.getQRCode("TIMO", amount, dto.getAppointmentId());
				paymentUrl = qrCodeResponse.getQrCodeUrl();
				payment.setPaymentUrl(paymentUrl);
			} catch (Exception ignored) {
			}
		}

		case CASH -> {
			transactionId = "CASH_" + dto.getAppointmentId() + "_" + System.currentTimeMillis();
			payment.setTransactionId(transactionId);
			payment.setStatus(Appointments_Enum.CONFIRMED);
		}
		}

		payment = paymentsRepository.save(payment);

		PaymentResponseDTO response = paymentMapper.toDTO(payment);
		response.setPaymentUrl(paymentUrl);
		return response;
	}

	@Override
	@Transactional
	public PaymentResponseDTO handleMomoCallback(Map<String, String> callbackParams) {
		// Lấy orderId từ callback
		String orderId = callbackParams.get("orderId");
		if (orderId == null) {
			throw new IllegalArgumentException("orderId không được bỏ trống");
		}

		// Tìm payment theo transactionId (orderId)
		Payments payment = paymentsRepository.findByTransactionId(orderId)
				.orElseThrow(() -> new NotFoundException("payment", "Payment not found"));

		// Verify signature
		String signature = callbackParams.get("signature");
		if (!momoUtils.verifySignature(callbackParams, signature)) {
			throw new SecurityException("Signature không hợp lệ");
		}

		// Lấy resultCode
		Integer resultCode = Integer.parseInt(callbackParams.get("resultCode"));

		// Cập nhật payment
		payment.setGatewayTransactionNo(callbackParams.get("transId"));
		payment.setResponseCode(String.valueOf(resultCode));
		payment.setSecureHash(signature);

		if (resultCode == 0) {
			// Thanh toán thành công
			payment.setStatus(Appointments_Enum.CONFIRMED);
			// Cập nhật appointment status
			Appointments appointment = payment.getAppointments();
			if (appointment.getStatus() == Appointments_Enum.PENDING) {
				appointment.setStatus(Appointments_Enum.CONFIRMED);
				appointmentsRepository.save(appointment);
			}
		} else {
			// Thanh toán thất bại
			payment.setStatus(Appointments_Enum.CANCELLED);
		}

		payment = paymentsRepository.save(payment);
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

		// Kiểm tra quyền: user chỉ xem được payment của chính mình, trừ ADMIN/EMPLOYEE
		// Logic: isActive = 0 (false) = đang hoạt động, isActive = 1 (true) = ngưng
		boolean isAdminOrEmployee = user.getRole() != null && !user.getRole().getIsActive() && // Role phải đang hoạt
																								// động (isActive =
																								// false)
				(user.getRole().getRoleName().equals("ADMIN") || user.getRole().getRoleName().equals("EMPLOYEE"));

		if (!isAdminOrEmployee && !payment.getAppointments().getUser().getUserId().equals(user.getUserId())) {
			throw new AccessDeniedException("Bạn không có quyền xem payment này");
		}

		return paymentMapper.toDTO(payment);
	}

	@Override
	public PagedResult<PaymentResponseDTO> getPaymentsByAppointment(Integer appointmentId, String username,
			Pageable pageable) {
		Appointments appointment = appointmentsRepository.findById(appointmentId)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment not found"));

		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User not found"));

		// Kiểm tra quyền
		// Logic: isActive = 0 (false) = đang hoạt động, isActive = 1 (true) = ngưng
		boolean isAdminOrEmployee = user.getRole() != null && !user.getRole().getIsActive() && // Role phải đang hoạt
																								// động (isActive =
																								// false)
				(user.getRole().getRoleName().equals("ADMIN") || user.getRole().getRoleName().equals("EMPLOYEE"));

		if (!isAdminOrEmployee && !appointment.getUser().getUserId().equals(user.getUserId())) {
			throw new AccessDeniedException("Bạn không có quyền xem payment của appointment này");
		}

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
		// Validate input
		if (callbackDTO.getContent() == null || callbackDTO.getContent().isEmpty()) {
			throw new IllegalArgumentException("Nội dung chuyển khoản không được bỏ trống");
		}
		if (callbackDTO.getAmount() == null || callbackDTO.getAmount() <= 0) {
			throw new IllegalArgumentException("Số tiền không hợp lệ");
		}

		// Parse appointmentId từ nội dung chuyển khoản
		// Format: "COC_LK_1" -> appointmentId = 1
		Integer appointmentId = null;
		try {
			String content = callbackDTO.getContent();
			// Tìm pattern "COC_LK_" hoặc "LK_" hoặc chỉ số
			if (content.contains("COC_LK_")) {
				String[] parts = content.split("COC_LK_");
				if (parts.length > 1) {
					// Lấy số sau "COC_LK_"
					String idStr = parts[1].trim();
					// Loại bỏ các ký tự không phải số ở đầu
					idStr = idStr.replaceAll("^[^0-9]+", "");
					appointmentId = Integer.parseInt(idStr);
				}
			} else if (content.contains("LK_")) {
				String[] parts = content.split("LK_");
				if (parts.length > 1) {
					String idStr = parts[1].trim();
					idStr = idStr.replaceAll("^[^0-9]+", "");
					appointmentId = Integer.parseInt(idStr);
				}
			} else {
				// Thử parse trực tiếp số từ content
				String idStr = content.replaceAll("[^0-9]", "");
				if (!idStr.isEmpty()) {
					appointmentId = Integer.parseInt(idStr);
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Không thể parse appointmentId từ nội dung chuyển khoản: " + callbackDTO.getContent());
		}

		if (appointmentId == null) {
			throw new IllegalArgumentException("Không tìm thấy appointmentId trong nội dung chuyển khoản");
		}

		// Tìm payment theo appointmentId, paymentMethod = BANK_TRANSFER, status =
		// PENDING
		List<Payments> pendingPayments = paymentsRepository.findByAppointments_Id(appointmentId).stream().filter(
				p -> p.getPaymentMethod() == PaymentMethod.BANK_TRANSFER && p.getStatus() == Appointments_Enum.PENDING)
				.sorted((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate())) // Lấy payment mới nhất
				.toList();

		if (pendingPayments.isEmpty()) {
			throw new NotFoundException("payment",
					"Không tìm thấy payment đang chờ thanh toán cho appointment " + appointmentId);
		}

		// Tìm payment có số tiền khớp (cho phép sai số nhỏ do làm tròn)
		Payments matchedPayment = null;
		double tolerance = 0.01; // Cho phép sai số 1 xu

		for (Payments payment : pendingPayments) {
			if (Math.abs(payment.getAmount() - callbackDTO.getAmount()) <= tolerance) {
				matchedPayment = payment;
				break;
			}
		}

		if (matchedPayment == null) {
			throw new IllegalArgumentException("Không tìm thấy payment với số tiền khớp. Số tiền trong hệ thống: "
					+ pendingPayments.get(0).getAmount() + ", Số tiền nhận được: " + callbackDTO.getAmount());
		}

		// Cập nhật payment status
		matchedPayment.setStatus(Appointments_Enum.CONFIRMED);
		if (callbackDTO.getBankTransactionId() != null) {
			matchedPayment.setGatewayTransactionNo(callbackDTO.getBankTransactionId());
		}
		if (callbackDTO.getFromAccount() != null) {
			// Ghi nhận thông tin người chuyển trên bank statement cùng fullname trong hệ
			// thống
			String responseCodeValue = "STK: " + callbackDTO.getFromAccount();
			String payerName = matchedPayment.getAppointments().getUser() != null
					? matchedPayment.getAppointments().getUser().getFullName()
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
			matchedPayment.setResponseCode(responseCodeValue);
		}

		// Lưu và flush ngay để đảm bảo thay đổi được ghi vào database
		matchedPayment = paymentsRepository.saveAndFlush(matchedPayment);

		// Cập nhật appointment status nếu đang PENDING
		Appointments appointment = matchedPayment.getAppointments();
		if (appointment.getStatus() == Appointments_Enum.PENDING
				|| appointment.getStatus() == Appointments_Enum.AWAITING_DEPOSIT) {
			appointment.setStatus(Appointments_Enum.CONFIRMED);
			appointmentsRepository.saveAndFlush(appointment);
		}

		// Reload entity từ database để đảm bảo đọc đúng giá trị mới nhất
		matchedPayment = paymentsRepository.findById(matchedPayment.getId())
				.orElseThrow(() -> new NotFoundException("payment", "Payment not found after update"));

		return paymentMapper.toDTO(matchedPayment);
	}

	private PagedResult<PaymentResponseDTO> convertToPagedResult(Page<Payments> paymentsPage) {
		return PagedResult.<PaymentResponseDTO>builder().content(paymentsPage.map(paymentMapper::toDTO).toList())
				.totalElements((int) paymentsPage.getTotalElements()).totalPages(paymentsPage.getTotalPages())
				.currentPage(paymentsPage.getNumber()).pageSize(paymentsPage.getSize()).build();
	}

	private String convertMapToJsonString(Map<String, String> map) {
		try {
			return objectMapper.writeValueAsString(map);
		} catch (Exception e) {
			throw new RuntimeException("Error converting Map to JSON", e);
		}
	}
}
