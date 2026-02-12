package truonggg.service.appointment.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import truonggg.Enum.Appointments_Enum;
import truonggg.Exception.NotFoundException;
import truonggg.Model.Appointments;
import truonggg.Model.Doctors;
import truonggg.Model.User;
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.requestDTO.AppointmentsRequestDTO;
import truonggg.dto.requestDTO.AppointmentsUpdateRequestDTO;
import truonggg.mapper.AppointmentsMapper;
import truonggg.repo.AppointmentsRepository;
import truonggg.repo.DoctorsRepository;
import truonggg.repo.SchedulesRepository;
import truonggg.repo.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.appointment.AppointmentsCommandService;
import truonggg.service.appointment.AppointmentsQueryService;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentsCommandService, AppointmentsQueryService {

	private final AppointmentsRepository appointmentsRepository;
	private final UserRepository userRepository;
	private final DoctorsRepository doctorsRepository;
	private final AppointmentsMapper appointmentsMapper;
	private final SchedulesRepository schedulesRepository;

	private static final int APPOINTMENT_DURATION_MINUTES = 30;

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
		// Chặn đặt lịch ở quá khứ (phòng khi validation @Future bị bỏ qua)
		LocalDateTime appointmentTime = dto.getAppointmentDateTime();
		if (appointmentTime == null) {
			throw new IllegalArgumentException("Thời gian không được để trống");
		}

		if (appointmentTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Thời gian đặt lịch phải ở hiện tại hoặc tương lai");
		}

		validateSlotFormat(appointmentTime);

		// Lấy user
		User user = this.userRepository.findById(currentUserId)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		Doctors doctors = null;
		if (dto.getDoctorId() != null) {
			doctors = this.doctorsRepository.findById(dto.getDoctorId())
					.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

			validateAppointmentTime(doctors.getId(), appointmentTime, null);

		}

		// Tạo lịch hẹn
		Appointments appointment = new Appointments();
		appointment.setUser(user);
		appointment.setDoctors(doctors);
		appointment.setAppointmentDateTime(appointmentTime);
		appointment.setNote(dto.getNote());
		appointment.setStatus(Appointments_Enum.PENDING);

		return this.appointmentsMapper.toDTO(this.appointmentsRepository.save(appointment));
	}

	@Override
	public AppointmentsResponseDTO update(Integer id, AppointmentsUpdateRequestDTO dto) {
		// Tìm xem có lịch hẹn không
		Appointments foundAppointment = this.appointmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment Not Found"));

		// Chuẩn bị giá trị cuối cùng sau update để validate trùng lịch / lịch làm việc
		LocalDateTime newTime = dto.getAppointmentDateTime() != null ? dto.getAppointmentDateTime()
				: foundAppointment.getAppointmentDateTime();
		Integer newDoctorId = dto.getDoctorId() != null ? dto.getDoctorId()
				: (foundAppointment.getDoctors() != null ? foundAppointment.getDoctors().getId() : null);

		if (newTime != null && newTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Không thể cập nhật lịch về quá khứ");
		}

		// Nếu có thay đổi thời gian hoặc bác sĩ, validate lại
		if (newDoctorId != null && newTime != null
				&& (dto.getAppointmentDateTime() != null || dto.getDoctorId() != null)) {

			validateAppointmentTime(newDoctorId, newTime, id);
		}

		// Cập nhật thông tin nếu có
		if (dto.getAppointmentDateTime() != null) {
			foundAppointment.setAppointmentDateTime(dto.getAppointmentDateTime());
		}
		if (dto.getStatus() != null) {
			foundAppointment.setStatus(dto.getStatus());
		}
		if (dto.getNote() != null) {
			foundAppointment.setNote(dto.getNote());
		}

		// Nếu có thay đổi doctor
		if (dto.getDoctorId() != null) {
			Doctors doctors = this.doctorsRepository.findById(dto.getDoctorId())
					.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));
			foundAppointment.setDoctors(doctors);
		}

		// Nếu có thay đổi user
		if (dto.getUserId() != null) {
			User user = this.userRepository.findById(dto.getUserId())
					.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
			foundAppointment.setUser(user);
		}

		return this.appointmentsMapper.toDTO(this.appointmentsRepository.save(foundAppointment));
	}

	@Override
	public AppointmentsResponseDTO delete(Integer id) {
		// Tìm xem có lịch hẹn không
		Appointments foundAppointment = this.appointmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment Not Found"));

		// Soft delete - cập nhật status thành CANCELLED
		foundAppointment.setStatus(Appointments_Enum.CANCELLED);
		return this.appointmentsMapper.toDTO(this.appointmentsRepository.save(foundAppointment));
	}

	@Override
	@Transactional
	public boolean deleteManually(Integer id) {
		// Tìm xem có lịch hẹn không
		Appointments foundAppointment = this.appointmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment Not Found"));
		foundAppointment.getList().clear();
		if (foundAppointment.getDoctors() != null) {
			foundAppointment.getDoctors().getList().remove(foundAppointment);
		}
		// Hard delete - xóa hoàn toàn khỏi DB
		this.appointmentsRepository.delete(foundAppointment);

		return true;
	}

	@Override
	public AppointmentsResponseDTO cancelByUser(Integer id, String userName) {
		Appointments found = appointmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment Not Found"));

		if (!found.getUser().getUserName().equals(userName)) {
			throw new AccessDeniedException("You cannot cancel this appointment");
		}

		// Không cho hủy nếu đã COMPLETED hoặc đã bị hủy trước đó
		if (found.getStatus() == Appointments_Enum.COMPLETED || found.getStatus() == Appointments_Enum.CANCELLED
				|| found.getStatus() == Appointments_Enum.CANCELLED_NO_REFUND
				|| found.getStatus() == Appointments_Enum.CANCELLED_REFUND) {
			throw new IllegalArgumentException("Không thể hủy lịch hẹn ở trạng thái hiện tại");
		}

		found.setStatus(Appointments_Enum.CANCELLED);
		return appointmentsMapper.toDTO(appointmentsRepository.save(found));
	}

	@Override
	public AppointmentsResponseDTO assignDoctor(Integer appointmentId, Integer doctorId) {
		Appointments appointment = this.appointmentsRepository.findById(appointmentId)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment Not Found"));

		// Không cho gán bác sĩ cho lịch đã hoàn thành / đã hủy
		if (appointment.getStatus() == Appointments_Enum.COMPLETED
				|| appointment.getStatus() == Appointments_Enum.CANCELLED
				|| appointment.getStatus() == Appointments_Enum.CANCELLED_NO_REFUND
				|| appointment.getStatus() == Appointments_Enum.CANCELLED_REFUND) {
			throw new IllegalArgumentException("Không thể gán bác sĩ cho lịch hẹn ở trạng thái hiện tại");
		}

		Doctors doctor = this.doctorsRepository.findById(doctorId)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		if (appointment.getDoctors() != null && appointment.getDoctors().getId().equals(doctorId)) {
			return this.appointmentsMapper.toDTO(appointment);
		}

		validateAppointmentTime(doctorId, appointment.getAppointmentDateTime(), appointment.getId());

		appointment.setDoctors(doctor);
		if (appointment.getStatus() == null || appointment.getStatus() == Appointments_Enum.PENDING) {
			appointment.setStatus(Appointments_Enum.CONFIRMED);
		}

		return this.appointmentsMapper.toDTO(this.appointmentsRepository.save(appointment));
	}

	// ================= VALIDATION HELPERS =================

	private void validateAppointmentTime(Integer doctorId, LocalDateTime startTime, Integer excludeAppointmentId) {

		if (startTime == null) {
			throw new IllegalArgumentException("Thời gian không hợp lệ");
		}

// 0️ Check slot 30 phút
		validateSlotFormat(startTime);

// 1️ Check thuộc ca làm việc
		boolean hasSchedule = schedulesRepository
				.existsByDoctors_IdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(doctorId, startTime, startTime);

		if (!hasSchedule) {
			throw new IllegalArgumentException("Thời gian này không nằm trong ca làm việc của bác sĩ");
		}

// 2️ Check nghỉ trưa (12:00 - 13:00)
		LocalDateTime lunchStart = startTime.toLocalDate().atTime(12, 0);
		LocalDateTime lunchEnd = startTime.toLocalDate().atTime(13, 0);

		if (!startTime.isBefore(lunchStart) && startTime.isBefore(lunchEnd)) {
			throw new IllegalArgumentException("Không thể đặt lịch trong giờ nghỉ trưa (12:00 - 13:00)");
		}

// 3️ Check trùng slot (ĐÚNG với entity hiện tại)
		boolean doctorBusy;

		if (excludeAppointmentId == null) {
			doctorBusy = appointmentsRepository.existsByDoctors_IdAndAppointmentDateTimeAndStatusNot(doctorId,
					startTime, Appointments_Enum.CANCELLED);
		} else {
			doctorBusy = appointmentsRepository.existsByDoctors_IdAndAppointmentDateTimeAndStatusNotAndIdNot(doctorId,
					startTime, Appointments_Enum.CANCELLED, excludeAppointmentId);
		}

		if (doctorBusy) {
			throw new IllegalArgumentException("Slot này đã có người đặt");
		}
	}

	private void validateSlotFormat(LocalDateTime time) {

		if (time.getMinute() != 0 && time.getMinute() != 30) {
			throw new IllegalArgumentException("Chỉ được đặt lịch theo khung 30 phút (vd: 08:00, 08:30)");
		}

		if (time.getSecond() != 0 || time.getNano() != 0) {
			throw new IllegalArgumentException("Thời gian không hợp lệ");
		}
	}

}
