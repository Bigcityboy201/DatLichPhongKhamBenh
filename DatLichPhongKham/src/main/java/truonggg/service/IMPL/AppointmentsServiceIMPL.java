package truonggg.service.IMPL;

import java.util.List;
import java.util.stream.Collectors;

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
import truonggg.repo.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.AppointmentsService;

@Service
@RequiredArgsConstructor
public class AppointmentsServiceIMPL implements AppointmentsService {
	private final AppointmentsRepository appointmentsRepository;
	private final UserRepository userRepository;
	private final DoctorsRepository doctorsRepository;
	private final AppointmentsMapper appointmentsMapper;

	@Override
	public AppointmentsResponseDTO createAppointments(AppointmentsRequestDTO dto) {
		User user = this.userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		Doctors doctors = null;
		if (dto.getDoctorId() != null) {
			doctors = this.doctorsRepository.findById(dto.getDoctorId())
					.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

			boolean doctorBusy = this.appointmentsRepository
					.existsByDoctors_IdAndAppointmentDateTimeAndStatusNot(doctors.getId(),
							dto.getAppointmentDateTime(), Appointments_Enum.CANCELLED);
			if (doctorBusy) {
				throw new IllegalArgumentException("Bác sĩ đã có lịch hẹn tại khung giờ này");
			}
		}

		Appointments appointments = this.appointmentsMapper.toEntity(dto);
		appointments.setUser(user);

		if (doctors != null) {
			appointments.setDoctors(doctors);
			if (appointments.getStatus() == null || appointments.getStatus() == Appointments_Enum.PENDING) {
				appointments.setStatus(Appointments_Enum.CONFIRMED);
			}
		} else {
			appointments.setDoctors(null);
			appointments.setStatus(Appointments_Enum.PENDING);
		}

		return this.appointmentsMapper.toDTO(this.appointmentsRepository.save(appointments));
	}

	@Override
	public List<AppointmentsResponseDTO> getAll() {
		List<Appointments> appointments = this.appointmentsRepository.findAll();
		return this.appointmentsMapper.toDTOList(appointments);
	}

	@Override
	public PagedResult<AppointmentsResponseDTO> getAllPaged(Pageable pageable) {
		Page<Appointments> appointmentsPage = this.appointmentsRepository.findAll(pageable);
		List<AppointmentsResponseDTO> dtoList = appointmentsPage.stream().map(appointmentsMapper::toDTO)
				.collect(Collectors.toList());

		return PagedResult.<AppointmentsResponseDTO>builder().content(dtoList)
				.totalElements((int) appointmentsPage.getTotalElements()).totalPages(appointmentsPage.getTotalPages())
				.currentPage(appointmentsPage.getNumber()).pageSize(appointmentsPage.getSize()).build();
	}

	@Override
	public AppointmentsResponseDTO findById(Integer id) {
		Appointments appointment = this.appointmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment Not Found"));
		return this.appointmentsMapper.toDTO(appointment);
	}

	@Override
	public AppointmentsResponseDTO update(Integer id, AppointmentsUpdateRequestDTO dto) {
		// Tìm xem có lịch hẹn không
		Appointments foundAppointment = this.appointmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment Not Found"));

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
	public PagedResult<AppointmentsResponseDTO> getAppointmentByCurrentUser(String userName, Pageable pageable) {
		User user = this.userRepository.findByUserName(userName)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
		Page<Appointments> appointmentsPage = appointmentsRepository.findByUser_UserId(user.getUserId(), pageable);
		List<AppointmentsResponseDTO> dtoList = appointmentsPage.stream().map(appointmentsMapper::toDTO)
				.collect(Collectors.toList());

		PagedResult<AppointmentsResponseDTO> pagedResult = PagedResult.<AppointmentsResponseDTO>builder()
				.content(dtoList).totalElements((int) appointmentsPage.getTotalElements())
				.totalPages(appointmentsPage.getTotalPages()).currentPage(appointmentsPage.getNumber())
				.pageSize(appointmentsPage.getSize()).build();

		return pagedResult;
	}

	@Override
	public AppointmentsResponseDTO cancelByUser(Integer id, String userName) {
		Appointments found = appointmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment Not Found"));
		if (!found.getUser().getUserName().equals(userName)) {
			throw new AccessDeniedException("You cannot cancel this appointment");
		}
		found.setStatus(Appointments_Enum.CANCELLED);
		return appointmentsMapper.toDTO(appointmentsRepository.save(found));
	}

	@Override
	public AppointmentsResponseDTO assignDoctor(Integer appointmentId, Integer doctorId) {
		Appointments appointment = this.appointmentsRepository.findById(appointmentId)
				.orElseThrow(() -> new NotFoundException("appointment", "Appointment Not Found"));

		Doctors doctor = this.doctorsRepository.findById(doctorId)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		if (appointment.getDoctors() != null && appointment.getDoctors().getId().equals(doctorId)) {
			return this.appointmentsMapper.toDTO(appointment);
		}

		boolean doctorBusy = this.appointmentsRepository
				.existsByDoctors_IdAndAppointmentDateTimeAndStatusNotAndIdNot(doctorId,
						appointment.getAppointmentDateTime(), Appointments_Enum.CANCELLED, appointment.getId());
		if (doctorBusy) {
			throw new IllegalArgumentException("Bác sĩ đã có lịch hẹn tại khung giờ này");
		}

		appointment.setDoctors(doctor);
		if (appointment.getStatus() == null || appointment.getStatus() == Appointments_Enum.PENDING) {
			appointment.setStatus(Appointments_Enum.CONFIRMED);
		}

		return this.appointmentsMapper.toDTO(this.appointmentsRepository.save(appointment));
	}
}
