package truonggg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import truonggg.Enum.Appointments_Enum;
import truonggg.Exception.NotFoundException;
import truonggg.Model.Appointments;
import truonggg.Model.Doctors;
import truonggg.Model.User;
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.requestDTO.AppointmentsRequestDTO;
import truonggg.mapper.AppointmentsMapper;
import truonggg.repo.AppointmentsRepository;
import truonggg.repo.DoctorsRepository;
import truonggg.repo.SchedulesRepository;
import truonggg.repo.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.appointment.impl.AppointmentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

	@Mock
	private AppointmentsRepository appointmentsRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private DoctorsRepository doctorsRepository;

	@Mock
	private AppointmentsMapper appointmentsMapper;

	@Mock
	private SchedulesRepository schedulesRepository;

	@InjectMocks
	private AppointmentServiceImpl appointmentService;

	// ============= getAllPaged ============
	@DisplayName("getAllPaged: success")
	@Test
	void getAllPaged_ShouldReturnPagedResult() {
		Pageable pageable = PageRequest.of(0, 2);

		Appointments a1 = new Appointments();
		Appointments a2 = new Appointments();
		Page<Appointments> page = new PageImpl<>(List.of(a1, a2), pageable, 2);

		when(appointmentsRepository.findAll(pageable)).thenReturn(page);
		when(appointmentsMapper.toDTOList(page.getContent()))
				.thenReturn(List.of(new AppointmentsResponseDTO(), new AppointmentsResponseDTO()));

		PagedResult<AppointmentsResponseDTO> result = appointmentService.getAllPaged(pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		verify(appointmentsRepository).findAll(pageable);
	}

	// ============= createAppointments ============
	@DisplayName("createAppointments: success when future time and user/doctor valid")
	@Test
	void createAppointments_ShouldCreate_WhenFutureTimeAndValid() {
		AppointmentsRequestDTO dto = new AppointmentsRequestDTO();
		// dto.setUserId(1);
		dto.setDoctorId(2);
		LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0)
				.withNano(0);

		dto.setAppointmentDateTime(appointmentTime);

		Integer currentUserId = 1;
		User user = new User();
		user.setUserId(1);
		Doctors doctor = new Doctors();
		doctor.setId(2);

		Appointments saved = new Appointments();
		AppointmentsResponseDTO responseDTO = new AppointmentsResponseDTO();

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(doctorsRepository.findById(2)).thenReturn(Optional.of(doctor));

		when(schedulesRepository.existsByDoctors_IdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(eq(2),
				any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(true);
		when(appointmentsRepository.existsByDoctors_IdAndAppointmentDateTimeAndStatusNot(eq(2),
				any(LocalDateTime.class), eq(Appointments_Enum.CANCELLED))).thenReturn(false);

		when(appointmentsRepository.save(any(Appointments.class))).thenReturn(saved);
		when(appointmentsMapper.toDTO(saved)).thenReturn(responseDTO);

		AppointmentsResponseDTO result = appointmentService.createAppointments(dto, currentUserId);

		assertNotNull(result);
		verify(userRepository).findById(1);
		verify(doctorsRepository).findById(2);
		verify(appointmentsRepository).save(any(Appointments.class));
	}

	@DisplayName("createAppointments: throw IllegalArgumentException when time in the past")
	@Test
	void createAppointments_ShouldThrow_WhenTimeInPast() {
		AppointmentsRequestDTO dto = new AppointmentsRequestDTO();
		// dto.setUserId(1);
		dto.setAppointmentDateTime(LocalDateTime.now().minusHours(1));

		Integer currentUserId = 1;
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.createAppointments(dto, currentUserId));
		assertEquals("Thời gian đặt lịch phải ở hiện tại hoặc tương lai", ex.getMessage());

		verify(userRepository, never()).findById(any());
	}

	@DisplayName("createAppointments: throw NotFoundException when user not found")
	@Test
	void createAppointments_ShouldThrow_WhenUserNotFound() {
		AppointmentsRequestDTO dto = new AppointmentsRequestDTO();
		// dto.setUserId(1);
		Integer currentUserId = 1;
		dto.setAppointmentDateTime(LocalDateTime.of(2026, 3, 10, 10, 0));

		when(userRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class,
				() -> appointmentService.createAppointments(dto, currentUserId));
		assertEquals("user: User Not Found", ex.getMessage());
	}

	// ============= cancelByUser ============
	@DisplayName("cancelByUser: success when owner and status allow cancel")
	@Test
	void cancelByUser_ShouldCancel_WhenOwnerAndStatusValid() {
		Integer id = 1;
		String username = "user1";

		User user = new User();
		user.setUserName(username);

		Appointments appointment = new Appointments();
		appointment.setId(id);
		appointment.setUser(user);
		appointment.setStatus(Appointments_Enum.PENDING);

		Appointments saved = new Appointments();
		saved.setStatus(Appointments_Enum.CANCELLED);

		when(appointmentsRepository.findById(id)).thenReturn(Optional.of(appointment));
		when(appointmentsRepository.save(appointment)).thenReturn(saved);
		when(appointmentsMapper.toDTO(saved)).thenReturn(new AppointmentsResponseDTO());

		AppointmentsResponseDTO result = appointmentService.cancelByUser(id, username);

		assertNotNull(result);
		verify(appointmentsRepository).save(appointment);
	}

	@DisplayName("cancelByUser: throw AccessDeniedException when not owner")
	@Test
	void cancelByUser_ShouldThrow_WhenNotOwner() {
		Integer id = 1;

		User user = new User();
		user.setUserName("other");

		Appointments appointment = new Appointments();
		appointment.setId(id);
		appointment.setUser(user);
		appointment.setStatus(Appointments_Enum.PENDING);

		when(appointmentsRepository.findById(id)).thenReturn(Optional.of(appointment));

		AccessDeniedException ex = assertThrows(AccessDeniedException.class,
				() -> appointmentService.cancelByUser(id, "user1"));
		assertEquals("You cannot cancel this appointment", ex.getMessage());
	}

	@DisplayName("cancelByUser: throw IllegalArgumentException when status not cancellable")
	@Test
	void cancelByUser_ShouldThrow_WhenStatusInvalid() {
		Integer id = 1;
		String username = "user1";

		User user = new User();
		user.setUserName(username);

		Appointments appointment = new Appointments();
		appointment.setId(id);
		appointment.setUser(user);
		appointment.setStatus(Appointments_Enum.COMPLETED);

		when(appointmentsRepository.findById(id)).thenReturn(Optional.of(appointment));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.cancelByUser(id, username));
		assertEquals("Không thể hủy lịch hẹn ở trạng thái hiện tại", ex.getMessage());
	}

	// ============= delete soft ============
	@DisplayName("delete (soft): success when appointment exists")
	@Test
	void deleteSoft_ShouldSetCancelled_WhenExists() {
		Integer id = 1;
		Appointments appointment = new Appointments();

		when(appointmentsRepository.findById(id)).thenReturn(Optional.of(appointment));
		when(appointmentsRepository.save(appointment)).thenReturn(appointment);
		when(appointmentsMapper.toDTO(appointment)).thenReturn(new AppointmentsResponseDTO());

		AppointmentsResponseDTO result = appointmentService.delete(id);

		assertNotNull(result);
		assertEquals(Appointments_Enum.CANCELLED, appointment.getStatus());
		verify(appointmentsRepository).save(appointment);
	}

	@DisplayName("delete (soft): throw NotFoundException when appointment not found")
	@Test
	void deleteSoft_ShouldThrow_WhenNotFound() {
		when(appointmentsRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> appointmentService.delete(1));
		assertEquals("appointment: Appointment Not Found", ex.getMessage());
	}

	// ============= delete hard ============
	@DisplayName("delete (hard): success when appointment exists")
	@Test
	void deleteHard_ShouldDelete_WhenExists() {
		Integer id = 1;
		Appointments appointment = new Appointments();

		when(appointmentsRepository.findById(id)).thenReturn(Optional.of(appointment));

		boolean result = appointmentService.deleteManually(id);

		assertTrue(result);
		verify(appointmentsRepository).delete(appointment);
	}

	@DisplayName("delete (hard): throw NotFoundException when appointment not found")
	@Test
	void deleteHard_ShouldThrow_WhenNotFound() {
		when(appointmentsRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> appointmentService.deleteManually(1));
		assertEquals("appointment: Appointment Not Found", ex.getMessage());

		verify(appointmentsRepository, never()).delete(any());
	}
}
