package truonggg.service.IMPL;

import java.util.Date;
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
import truonggg.dto.requestDTO.PaymentRequestDTO;
import truonggg.mapper.PaymentMapper;
import truonggg.repo.AppointmentsRepository;
import truonggg.repo.PaymentsRepository;
import truonggg.repo.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.PaymentService;
import truonggg.utils.MomoUtils;

@Service
@RequiredArgsConstructor
public class PaymentServiceIMPL implements PaymentService {
	
	private final PaymentsRepository paymentsRepository;
	private final AppointmentsRepository appointmentsRepository;
	private final UserRepository userRepository;
	private final PaymentMapper paymentMapper;
	private final MomoUtils momoUtils;
	private final ObjectMapper objectMapper;
	
	@Override
	@Transactional
	public PaymentResponseDTO createPayment(PaymentRequestDTO dto, String username) {
		// Lấy user
		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User not found"));
		
		// Lấy appointment
		Appointments appointment = appointmentsRepository.findById(dto.getAppointmentId())
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment not found"));
		
		// Kiểm tra quyền: user chỉ có thể thanh toán cho appointment của chính mình
		if (!appointment.getUser().getUserId().equals(user.getUserId())) {
			throw new AccessDeniedException("Bạn không có quyền thanh toán cho appointment này");
		}
		
		// Kiểm tra appointment status
		if (appointment.getStatus() == Appointments_Enum.CANCELLED) {
			throw new IllegalArgumentException("Không thể thanh toán cho appointment đã bị hủy");
		}
		
		// Validate amount > 0
		if (dto.getAmount() <= 0) {
			throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
		}
		
		// Validate payment method
		PaymentMethod paymentMethod;
		try {
			paymentMethod = PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ. Chỉ chấp nhận: MOMO, CASH, BANK_TRANSFER");
		}
		
		// Tạo payment record
		Payments payment = Payments.builder()
				.amount(dto.getAmount())
				.paymentDate(new Date())
				.paymentMethod(paymentMethod)
				.isDeposit(true) // Mặc định là đặt cọc
				.status(Appointments_Enum.PENDING)
				.appointments(appointment)
				.build();
		
		// Xử lý theo phương thức thanh toán
		String paymentUrl = null;
		String transactionId = null;
		
		if (dto.getPaymentMethod().toUpperCase().equals("MOMO")) {
			// Tạo orderId
			transactionId = momoUtils.generateOrderId(dto.getAppointmentId());
			payment.setTransactionId(transactionId);
			
			// Tạo payment request data từ MoMo
			Long amountLong = (long) (dto.getAmount() * 100); // MoMo yêu cầu amount tính bằng xu
			String orderInfo = "Thanh toan dat coc cho lich hen #" + dto.getAppointmentId();
			Map<String, String> paymentRequest = momoUtils.createPaymentRequest(transactionId, amountLong, orderInfo);
			
			// Lưu payment request data dưới dạng JSON string vào paymentUrl
			// Frontend sẽ dùng data này để gọi MoMo API
			paymentUrl = convertMapToJsonString(paymentRequest);
			payment.setPaymentUrl(paymentUrl);
		} else if (dto.getPaymentMethod().toUpperCase().equals("CASH") || dto.getPaymentMethod().toUpperCase().equals("BANK_TRANSFER")) {
			// Thanh toán offline - không cần URL
			transactionId = "OFFLINE_" + dto.getAppointmentId() + "_" + System.currentTimeMillis();
			payment.setTransactionId(transactionId);
			payment.setStatus(Appointments_Enum.CONFIRMED);
		} else {
			// Đảm bảo transactionId luôn được set (fallback)
			transactionId = "PAYMENT_" + dto.getAppointmentId() + "_" + System.currentTimeMillis();
			payment.setTransactionId(transactionId);
		}
		
		// Lưu payment
		payment = paymentsRepository.save(payment);
		
		// Map sang DTO
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
		boolean isAdminOrEmployee = user.getRole() != null && 
				(user.getRole().getRoleName().equals("ADMIN") || user.getRole().getRoleName().equals("EMPLOYEE"));
		
		if (!isAdminOrEmployee && !payment.getAppointments().getUser().getUserId().equals(user.getUserId())) {
			throw new AccessDeniedException("Bạn không có quyền xem payment này");
		}
		
		return paymentMapper.toDTO(payment);
	}
	
	@Override
	public PagedResult<PaymentResponseDTO> getPaymentsByAppointment(Integer appointmentId, String username, Pageable pageable) {
		Appointments appointment = appointmentsRepository.findById(appointmentId)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment not found"));
		
		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User not found"));
		
		// Kiểm tra quyền
		boolean isAdminOrEmployee = user.getRole() != null && 
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
	
	private PagedResult<PaymentResponseDTO> convertToPagedResult(Page<Payments> paymentsPage) {
		return PagedResult.<PaymentResponseDTO>builder()
				.content(paymentsPage.map(paymentMapper::toDTO).toList())
				.totalElements((int) paymentsPage.getTotalElements())
				.totalPages(paymentsPage.getTotalPages())
				.currentPage(paymentsPage.getNumber())
				.pageSize(paymentsPage.getSize())
				.build();
	}
	
	private String convertMapToJsonString(Map<String, String> map) {
		try {
			return objectMapper.writeValueAsString(map);
		} catch (Exception e) {
			throw new RuntimeException("Error converting Map to JSON", e);
		}
	}
}

