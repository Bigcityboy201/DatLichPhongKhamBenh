package truonggg.appointment.application.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import truonggg.Enum.PaymentStatus;
import truonggg.Exception.NotFoundException;
import truonggg.appointment.application.AppointmentsCommandService;
import truonggg.appointment.application.AppointmentsQueryService;
import truonggg.appointment.domain.model.Appointments;
import truonggg.appointment.domain.service.AppointmentCancellationService;
import truonggg.appointment.domain.service.AppointmentConflictValidator;
import truonggg.appointment.domain.service.AppointmentScheduleValidator;
import truonggg.appointment.infrastructure.AppointmentsRepository;
import truonggg.appointment.mapper.AppointmentsMapper;
import truonggg.doctor.domain.model.Doctors;
import truonggg.doctor.infrastructure.DoctorsRepository;
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.reponseDTO.CancelAppointmentResponse;
import truonggg.dto.requestDTO.AppointmentsRequestDTO;
import truonggg.dto.requestDTO.AppointmentsUpdateRequestDTO;
import truonggg.payment.domain.model.Payments;
import truonggg.payment.infrastructure.PaymentsRepository;
import truonggg.reponse.PagedResult;
import truonggg.user.domain.model.User;
import truonggg.user.infrastructure.UserRepository;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentsCommandService, AppointmentsQueryService {

	private final AppointmentsRepository appointmentsRepository;
	private final UserRepository userRepository;
	private final DoctorsRepository doctorsRepository;
	private final AppointmentsMapper appointmentsMapper;
	private final PaymentsRepository paymentsRepository;
    private final AppointmentScheduleValidator scheduleValidator;
    private final AppointmentConflictValidator conflictValidator;
    private final AppointmentCancellationService appointmentCancellationService;


	// ================= QUERY =================

	@Override
	public PagedResult<AppointmentsResponseDTO> getAllPaged(Pageable pageable) {
		Page<Appointments> appointmentsPage = this.appointmentsRepository.findAll(pageable);
		List<AppointmentsResponseDTO> dtoList = appointmentsMapper.toDTOList(appointmentsPage.getContent());

		return PagedResult.from(appointmentsPage, dtoList);
	}

	@Override
	public AppointmentsResponseDTO findById(Integer id) {
		Appointments appointment = this.appointmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment Not Found"));
		return this.appointmentsMapper.toDTO(appointment);
	}

	@Override
	public PagedResult<AppointmentsResponseDTO> getAppointmentByCurrentUser(String userName, Pageable pageable) {
		User user = this.userRepository.findByUserName(userName)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
		Page<Appointments> appointmentsPage = appointmentsRepository.findByUser_UserId(user.getUserId(), pageable);
		List<AppointmentsResponseDTO> dtoList = appointmentsMapper.toDTOList(appointmentsPage.getContent());

		return PagedResult.from(appointmentsPage, dtoList);
	}

	// ================= COMMAND =================

	@Override
	public AppointmentsResponseDTO createAppointments(AppointmentsRequestDTO dto, Integer currentUserId) {
		LocalDateTime appointmentTime = dto.getAppointmentDateTime();

		// Lấy user
		User user = this.userRepository.findById(currentUserId)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		Doctors doctors = null;
		if (dto.getDoctorId() != null) {
			doctors = this.doctorsRepository.findById(dto.getDoctorId())
					.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

            scheduleValidator.validate(doctors.getId(),
                    dto.getAppointmentDateTime());
            conflictValidator.validate(doctors.getId(),
                    dto.getAppointmentDateTime(), null);

		}

		// Tạo lịch hẹn (áp dụng rule thời gian ở entity)
		Appointments appointment = Appointments.create(user, doctors, appointmentTime, dto.getNote());

		return this.appointmentsMapper.toDTO(this.appointmentsRepository.save(appointment));
	}

    @Override
    public AppointmentsResponseDTO update(
            Integer id,
            AppointmentsUpdateRequestDTO dto) {

        Appointments appointment =
                appointmentsRepository.findById(id)
                        .orElseThrow(() ->
                                new NotFoundException("appointment", "Not Found"));

        if (dto.getAppointmentDateTime() != null) {

            scheduleValidator.validate(
                    appointment.getDoctors().getId(),
                    dto.getAppointmentDateTime());

            conflictValidator.validate(
                    appointment.getDoctors().getId(),
                    dto.getAppointmentDateTime(),
                    id);

            appointment.reschedule(
                    dto.getAppointmentDateTime());
        }

        if (dto.getDoctorId() != null) {

            Doctors doctor =
                    doctorsRepository.findById(dto.getDoctorId())
                            .orElseThrow(() ->
                                    new NotFoundException("doctor", "Not Found"));

            appointment.assignDoctor(doctor);
        }

        if (dto.getNote() != null) {
            appointment.setNote(dto.getNote());
        }
        return this.appointmentsMapper.toDTO(
                appointmentsRepository.save(appointment));
    }

	@Override
	public AppointmentsResponseDTO delete(Integer id) {
		// Tìm xem có lịch hẹn không
		Appointments foundAppointment = this.appointmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment Not Found"));

		// Soft delete - cập nhật status thành CANCELLED
        foundAppointment.cancel();
		return this.appointmentsMapper.toDTO(this.appointmentsRepository.save(foundAppointment));
	}

    @Override
    @Transactional
    public CancelAppointmentResponse cancelByUser(Integer appointmentId, String username) {

        //2Lấy appointment
        Appointments appointment =
                appointmentsRepository.findById(appointmentId)
                        .orElseThrow(() ->
                                new NotFoundException("appointment", "Not Found"));

        //2Kiểm tra quyền sở hữu
        if (!appointment.getUser().getUserName().equals(username)) {
            throw new AccessDeniedException("Not allowed");
        }

        //3Domain tự kiểm tra có thể hủy không
        appointment.cancel();

        String message;

        //4Tìm payment deposit
        Optional<Payments> depositOpt = paymentsRepository.findByAppointmentsAndStatus(appointment, PaymentStatus.CONFIRMED);

        if (depositOpt.isPresent()) {

            Payments deposit = depositOpt.get();

            //5Gọi Domain Service để quyết định refund
            boolean shouldRefund = appointmentCancellationService.shouldRefund(deposit);

            if (shouldRefund) {

                deposit.refund();
                paymentsRepository.save(deposit);

                appointment.cancelWithRefund();
                message = "Cancelled with refund";

            } else {

                appointment.cancelWithoutRefund();
                message = "Cancelled without refund";
            }

        } else {

            appointment.cancelWithoutRefund();
            message = "Cancelled";
        }

        // 6Lưu appointment
        appointmentsRepository.save(appointment);

        return new CancelAppointmentResponse(
                appointmentsMapper.toDTO(appointment),
                message
        );
    }

    @Override
    public AppointmentsResponseDTO assignDoctor(
            Integer appointmentId,
            Integer doctorId) {

        Appointments appointment =
                appointmentsRepository.findById(appointmentId)
                        .orElseThrow(() ->
                                new NotFoundException("appointment", "Not Found"));

        Doctors doctor =
                doctorsRepository.findById(doctorId)
                        .orElseThrow(() ->
                                new NotFoundException("doctor", "Not Found"));

        scheduleValidator.validate(
                doctor.getId(),
                appointment.getAppointmentDateTime());

        conflictValidator.validate(
                doctor.getId(),
                appointment.getAppointmentDateTime(),
                appointmentId);

        appointment.assignDoctor(doctor);

        return this.appointmentsMapper.toDTO(
                appointmentsRepository.save(appointment));
    }
}
