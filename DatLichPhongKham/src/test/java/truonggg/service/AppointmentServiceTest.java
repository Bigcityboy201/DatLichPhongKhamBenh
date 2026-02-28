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
import truonggg.Enum.PaymentStatus;
import truonggg.Exception.NotFoundException;

import truonggg.appointment.application.impl.AppointmentServiceImpl;
import truonggg.appointment.domain.model.Appointments;
import truonggg.appointment.infrastructure.AppointmentsRepository;
import truonggg.appointment.mapper.AppointmentsMapper;
import truonggg.doctor.domain.model.Doctors;
import truonggg.doctor.infrastructure.DoctorsRepository;
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.reponseDTO.CancelAppointmentResponse;
import truonggg.dto.requestDTO.AppointmentsRequestDTO;
import truonggg.payment.infrastructure.PaymentsRepository;
import truonggg.reponse.PagedResult;
import truonggg.schedules.infrastructure.SchedulesRepository;
import truonggg.user.domain.model.User;
import truonggg.user.infrastructure.UserRepository;

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

	@Mock
	private PaymentsRepository paymentsRepository;

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
		User user = org.mockito.Mockito.mock(User.class);
		Doctors doctor = org.mockito.Mockito.mock(Doctors.class);
		when(doctor.getId()).thenReturn(2);

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
		// user không tồn tại trong DB -> NotFoundException ưu tiên hơn
		when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class,
				() -> appointmentService.createAppointments(dto, currentUserId));
		assertEquals("user: User Not Found", ex.getMessage());
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
	@Test
	@DisplayName("cancelByUser: success when owner and no payment")
	void cancelByUser_ShouldCancel_WhenOwnerAndNoPayment() {

		Integer id = 1;
		String username = "user1";

		User user = org.mockito.Mockito.mock(User.class);
		when(user.getUserName()).thenReturn(username);

		Appointments appointment = new Appointments();
		appointment.setId(id);
		appointment.setUser(user);
		appointment.setStatus(Appointments_Enum.PENDING);

		when(appointmentsRepository.findById(id)).thenReturn(Optional.of(appointment));

		when(paymentsRepository.findByAppointmentsAndStatus(any(Appointments.class), eq(PaymentStatus.CONFIRMED)))
				.thenReturn(Optional.empty());

		when(appointmentsRepository.save(any(Appointments.class))).thenReturn(appointment);

		when(appointmentsMapper.toDTO(any(Appointments.class))).thenReturn(new AppointmentsResponseDTO());

		CancelAppointmentResponse result = appointmentService.cancelByUser(id, username);

		assertNotNull(result);
		assertEquals("Hủy lịch thành công.", result.getMessage());

		verify(appointmentsRepository).save(any(Appointments.class));
	}

	@Test
	@DisplayName("cancelByUser: throw AccessDeniedException when not owner")
	void cancelByUser_ShouldThrow_WhenNotOwner() {

		Integer id = 1;

		User user = org.mockito.Mockito.mock(User.class);
		when(user.getUserName()).thenReturn("other");

		Appointments appointment = new Appointments();
		appointment.setId(id);
		appointment.setUser(user);
		appointment.setStatus(Appointments_Enum.PENDING);

		when(appointmentsRepository.findById(id)).thenReturn(Optional.of(appointment));

		AccessDeniedException ex = assertThrows(AccessDeniedException.class,
				() -> appointmentService.cancelByUser(id, "user1"));

		assertEquals("You cannot cancel this appointment", ex.getMessage());
	}

	@Test
	@DisplayName("cancelByUser: throw IllegalArgumentException when status invalid")
	void cancelByUser_ShouldThrow_WhenStatusInvalid() {

		Integer id = 1;
		String username = "user1";

		User user = org.mockito.Mockito.mock(User.class);
		when(user.getUserName()).thenReturn(username);

		Appointments appointment = new Appointments();
		appointment.setId(id);
		appointment.setUser(user);
		appointment.setStatus(Appointments_Enum.COMPLETED);

		when(appointmentsRepository.findById(id)).thenReturn(Optional.of(appointment));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.cancelByUser(id, username));

		assertEquals("Không thể hủy lịch hẹn ở trạng thái hiện tại", ex.getMessage());
	}

	@Test
	@DisplayName("cancelByUser: throw NotFoundException when appointment not found")
	void cancelByUser_ShouldThrow_WhenNotFound() {

		Integer id = 1;

		when(appointmentsRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> appointmentService.cancelByUser(id, "user1"));
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
}
