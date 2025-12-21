package truonggg.service.IMPL;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
public class PaymentServiceIMPL implements PaymentService {

	// Số tiền cọc mặc định cho thanh toán chuyển khoản (QR)
	private static final double DEFAULT_DEPOSIT_AMOUNT = 2000.0;

	private final PaymentsRepository paymentsRepository;
	private final AppointmentsRepository appointmentsRepository;
	private final UserRepository userRepository;
	private final PaymentMapper paymentMapper;
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

		// ==================== 🔥 [THÊM] KIỂM TRA PAYMENT SUCCESS ====================
		boolean hasSuccessPayment = paymentsRepository.existsByAppointmentsAndStatus(appointment,
				Appointments_Enum.CONFIRMED);

		if (hasSuccessPayment) {
			throw new IllegalStateException("Appointment này đã được thanh toán");
		}
		// ==================== 🔥 KẾT THÚC PHẦN THÊM ====================

		// ==================== 🔥 [THÊM] KIỂM TRA PAYMENT PENDING ====================
		Optional<Payments> pendingPaymentOpt = paymentsRepository.findByAppointmentsAndStatus(appointment,
				Appointments_Enum.PENDING);

		if (pendingPaymentOpt.isPresent()) {
			Payments pendingPayment = pendingPaymentOpt.get();

			// 👉 TRẢ LẠI QR CŨ – KHÔNG TẠO PAYMENT MỚI
			PaymentResponseDTO response = paymentMapper.toDTO(pendingPayment);
			response.setPaymentUrl(pendingPayment.getPaymentUrl());

			return response;
		}
		// ==================== 🔥 KẾT THÚC PHẦN THÊM ====================

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

		// Chỉ cho phép thanh toán chuyển khoản qua MB
		PaymentMethod paymentMethod = PaymentMethod.BANK_TRANSFER;

		if (dto.getPaymentMethod() != null && !dto.getPaymentMethod().isBlank()) {
			PaymentMethod requestedMethod;
			try {
				requestedMethod = PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ. Hệ thống chỉ hỗ trợ MB BANK.");
			}

			if (requestedMethod != PaymentMethod.BANK_TRANSFER) {
				throw new IllegalArgumentException("Hệ thống hiện chỉ hỗ trợ thanh toán chuyển khoản MB BANK.");
			}
		}

		double amount = DEFAULT_DEPOSIT_AMOUNT;
		if (amount <= 0) {
			throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
		}

		// ==================== 🔥 [GIỮ NGUYÊN] TẠO PAYMENT MỚI ====================
		Payments payment = Payments.builder().amount(amount).paymentDate(new Date()).paymentMethod(paymentMethod)
				.isDeposit(true).status(Appointments_Enum.PENDING).appointments(appointment).build();

		String transactionId = "BANK_MB_" + dto.getAppointmentId() + "_" + System.currentTimeMillis();
		payment.setTransactionId(transactionId);

		String paymentCode = "COCLK" + dto.getAppointmentId();
		payment.setPaymentCode(paymentCode);

		var qrCodeResponse = qrCodeService.getQRCode("MB", amount, dto.getAppointmentId());
		payment.setPaymentUrl(qrCodeResponse.getQrCodeUrl());

		payment = paymentsRepository.save(payment);

		PaymentResponseDTO response = paymentMapper.toDTO(payment);
		response.setPaymentUrl(payment.getPaymentUrl());
		return response;
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
		Payments payment = null;

		// 1️⃣ Khớp theo gatewayTransactionNo (tid từ bank)
		if (callbackDTO.getBankTransactionId() != null && !callbackDTO.getBankTransactionId().isEmpty()) {
			payment = paymentsRepository.findByGatewayTransactionNo(callbackDTO.getBankTransactionId()).orElse(null);
		}

		// 2️⃣ Nếu không tìm thấy, parse appointmentId từ content và tìm payment
		if (payment == null && callbackDTO.getContent() != null && !callbackDTO.getContent().isEmpty()) {
			Integer appointmentId = parseAppointmentIdFromContent(callbackDTO.getContent());

			if (appointmentId != null) {
				// Tìm payment theo appointmentId, amount, paymentMethod = BANK_TRANSFER, status
				// = PENDING
				List<Payments> pendingPayments = paymentsRepository
						.findByPaymentMethodAndAmountAndStatus(PaymentMethod.BANK_TRANSFER, callbackDTO.getAmount(),
								Appointments_Enum.PENDING)
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

		// ✅ Cập nhật trạng thái payment
		payment.setStatus(Appointments_Enum.CONFIRMED);

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
		return PagedResult.<PaymentResponseDTO>builder().content(paymentsPage.map(paymentMapper::toDTO).toList())
				.totalElements((int) paymentsPage.getTotalElements()).totalPages(paymentsPage.getTotalPages())
				.currentPage(paymentsPage.getNumber()).pageSize(paymentsPage.getSize()).build();
	}
}
