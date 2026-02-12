package truonggg.service.impl;

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
import truonggg.Enum.PaymentStatus;
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

		// B1:Kiểm tra user tồn tại
		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User not found"));

		// B2:Kiểm tra appointment tồn tại
		Appointments appointment = appointmentsRepository.findById(dto.getAppointmentId())
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment not found"));

		// B3:Kiểm tra appointment đã thanh toán thành công hay chưa
		boolean hasSuccessPayment = paymentsRepository.existsByAppointmentsAndStatus(appointment,
				PaymentStatus.CONFIRMED);

		if (hasSuccessPayment) {
			throw new IllegalStateException("Appointment này đã được thanh toán");
		}

		// B4:KIỂM TRA PAYMENT PENDING
		Optional<Payments> pendingPaymentOpt = paymentsRepository.findByAppointmentsAndStatus(appointment,
				PaymentStatus.PENDING);

		// B4.1:Kiểm tra trạng thái pending chưa,nếu pending thì lấy ra
		if (pendingPaymentOpt.isPresent()) {
			Payments pendingPayment = pendingPaymentOpt.get();

			// B4.2:Chuyển sang dto và set lại qr cũ
			PaymentResponseDTO response = paymentMapper.toDTO(pendingPayment);
			response.setPaymentUrl(pendingPayment.getPaymentUrl());

			return response;
		}

		// B5:Kiểm tra quyền admin hay employee
		boolean isAdminOrEmployee = user.getRole() != null && !user.getRole().getIsActive()
				&& (user.getRole().getRoleName().equals("ADMIN") || user.getRole().getRoleName().equals("EMPLOYEE"));

		// B5.1:Kiểm tra nếu không thuộc quyền admin or employee or user thì thông báo
		if (!isAdminOrEmployee && !appointment.getUser().getUserId().equals(user.getUserId())) {
			throw new AccessDeniedException("Bạn không có quyền thanh toán cho appointment này");
		}

		// B6:Kiểm tra trạng thái appointment
		if (appointment.getStatus() == Appointments_Enum.CANCELLED) {
			throw new IllegalArgumentException("Không thể thanh toán cho appointment đã bị hủy");
		}

		// B7: Nếu chưa hủy-> xác định phương thức thanh toán: CASH hoặc BANK_TRANSFER
		PaymentMethod paymentMethod;
		if (dto.getPaymentMethod() != null && !dto.getPaymentMethod().isBlank()) {
			try {
				paymentMethod = PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase());
				if (paymentMethod != PaymentMethod.CASH && paymentMethod != PaymentMethod.BANK_TRANSFER) {
					throw new IllegalArgumentException(
							"Phương thức thanh toán không hợp lệ. Chỉ hỗ trợ: CASH, BANK_TRANSFER");
				}
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(
						"Phương thức thanh toán không hợp lệ. Chỉ hỗ trợ: CASH, BANK_TRANSFER");
			}
			// B7.1:Nếu không gửi pttt->BANK_TRANSFER
		} else {
			// Mặc định là BANK_TRANSFER
			paymentMethod = PaymentMethod.BANK_TRANSFER;
		}

		// Sử dụng số tiền cọc mặc định
		double amount = DEFAULT_DEPOSIT_AMOUNT;

		// B8:Nếu Cash->Confirmed(Còn lại pending)
		PaymentStatus initialStatus = paymentMethod == PaymentMethod.CASH ? PaymentStatus.CONFIRMED // CASH được xác
																									// nhận ngay
				: PaymentStatus.PENDING; // BANK_TRANSFER cần chờ xác nhận

		// B9:Tạo đối tượng payment nhưng chưa saveDB
		Payments payment = Payments.builder().amount(amount).paymentDate(new Date()).paymentMethod(paymentMethod)
				.isDeposit(true).status(initialStatus).appointments(appointment).build();

		// B10:Tạo mã giao dịch(mỗi cuộc hẹn chỉ có 1 transaction)
		String transactionId = paymentMethod == PaymentMethod.CASH
				? "CASH_" + dto.getAppointmentId() + "_" + System.currentTimeMillis()
				: "BANK_MB_" + dto.getAppointmentId() + "_" + System.currentTimeMillis();
		// B10.1:setTransaction vào
		payment.setTransactionId(transactionId);

		// B11:Tạo nội dung chuyển khoản
		String paymentCode = "COCLK" + dto.getAppointmentId();
		// B11.1:
		payment.setPaymentCode(paymentCode);

		// Chỉ tạo QR code cho BANK_TRANSFER
		if (paymentMethod == PaymentMethod.BANK_TRANSFER) {
			var qrCodeResponse = qrCodeService.getQRCode("BANK_TRANSFER", amount, dto.getAppointmentId());
			payment.setPaymentUrl(qrCodeResponse.getQrCodeUrl());
		}

		payment = paymentsRepository.save(payment);

		// Cập nhật appointment status nếu là CASH
		if (paymentMethod == PaymentMethod.CASH && (appointment.getStatus() == Appointments_Enum.PENDING
				|| appointment.getStatus() == Appointments_Enum.AWAITING_DEPOSIT)) {
			appointment.setStatus(Appointments_Enum.CONFIRMED);
			appointmentsRepository.saveAndFlush(appointment);
		}

		PaymentResponseDTO response = paymentMapper.toDTO(payment);
		if (payment.getPaymentUrl() != null) {
			response.setPaymentUrl(payment.getPaymentUrl());
		}
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

		// ✅ Cập nhật trạng thái payment
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
}
