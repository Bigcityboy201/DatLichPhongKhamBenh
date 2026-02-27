package truonggg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Pageable;

import truonggg.Exception.NotFoundException;
import truonggg.doctor.application.impl.DoctorsServiceIMPL;
import truonggg.doctor.infrastructure.DoctorsRepository;
import truonggg.doctor.mapper.DoctorsMapper;
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.reponseDTO.DoctorSummaryResponseDTO;
import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.department.infrastructure.DepartmentsRepository;
import truonggg.reponse.PagedResult;
import truonggg.appointment.domain.model.Appointments;
import truonggg.appointment.infrastructure.AppointmentsRepository;
import truonggg.appointment.mapper.AppointmentsMapper;
import truonggg.doctor.domain.model.Doctors;
import truonggg.schedules.infrastructure.SchedulesRepository;
import truonggg.schedules.mapper.SchedulesMapper;
import truonggg.schedules.domain.model.Schedules;
import truonggg.user.domain.model.User;
import truonggg.user.infrastructure.UserRepository;
import truonggg.dto.requestDTO.DoctorsDeleteRequestDTO;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

	@Mock
	private DoctorsRepository doctorsRepository;

	@Mock
	private DoctorsMapper doctorsMapper;

	@Mock
	private UserRepository userRepository;

	@Mock
	private DepartmentsRepository departmentsRepository;

	@Mock
	private AppointmentsRepository appointmentsRepository;

	@Mock
	private AppointmentsMapper appointmentsMapper;

	@Mock
	private SchedulesRepository schedulesRepository;

	@Mock
	private SchedulesMapper schedulesMapper;

	@InjectMocks
	private DoctorsServiceIMPL doctorsService;

	// ============= getDoctorsByDepartmentPaged ============
	@DisplayName("getDoctorsByDepartmentPaged: success when department exists")
	@Test
	void getDoctorsByDepartmentPaged_ShouldReturnPagedResult_WhenDepartmentExists() {
		Integer depId = 1;
		Pageable pageable = TestPageConstants.PAGEABLE_0_2;

		Doctors d1 = org.mockito.Mockito.mock(Doctors.class);
		Doctors d2 = org.mockito.Mockito.mock(Doctors.class);

		Page<Doctors> page = new PageImpl<>(List.of(d1, d2), pageable, TestPageConstants.DEFAULT_SIZE);

		when(departmentsRepository.existsById(depId)).thenReturn(true);
		when(doctorsRepository.findByDepartmentsId(depId, pageable)).thenReturn(page);
		when(doctorsMapper.toDTOOther(d1)).thenReturn(new DoctorSummaryResponseDTO());
		when(doctorsMapper.toDTOOther(d2)).thenReturn(new DoctorSummaryResponseDTO());

		PagedResult<DoctorSummaryResponseDTO> result = doctorsService.getDoctorsByDepartmentPaged(depId, pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		verify(doctorsRepository).findByDepartmentsId(depId, pageable);
	}

	@DisplayName("getDoctorsByDepartmentPaged: throw NotFoundException when department not exists")
	@Test
	void getDoctorsByDepartmentPaged_ShouldThrow_WhenDepartmentNotExists() {
		Integer depId = 1;
		Pageable pageable = TestPageConstants.PAGEABLE_0_2;

		when(departmentsRepository.existsById(depId)).thenReturn(false);

		NotFoundException ex = assertThrows(NotFoundException.class,
				() -> doctorsService.getDoctorsByDepartmentPaged(depId, pageable));
		assertEquals("department: Department not found!", ex.getMessage());

		verify(doctorsRepository, never()).findByDepartmentsId(any(), any());
	}

	// ============= findById ============
	@DisplayName("findById: success when doctor exists")
	@Test
	void findById_ShouldReturnDoctor_WhenExists() {
		Doctors doctor = org.mockito.Mockito.mock(Doctors.class);
		when(doctorsRepository.findByIdWithSchedules(1)).thenReturn(Optional.of(doctor));
		when(doctorsMapper.toDTO(doctor)).thenReturn(new DoctorsReponseDTO());

		DoctorsReponseDTO result = doctorsService.findById(1);

		assertNotNull(result);
		verify(doctorsRepository).findByIdWithSchedules(1);
		verify(doctorsMapper).toDTO(doctor);
	}

	@DisplayName("findById: throw NotFoundException when doctor not found")
	@Test
	void findById_ShouldThrow_WhenDoctorNotFound() {
		when(doctorsRepository.findByIdWithSchedules(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> doctorsService.findById(1));
		assertEquals("doctor: Doctor Not Found!", ex.getMessage());
	}

	// ============= deleteManually ============
	@DisplayName("deleteManually: success when doctor and user exist")
	@Test
	void deleteManually_ShouldDeleteDoctorAndUser_WhenExist() {
		Doctors doctor = org.mockito.Mockito.mock(Doctors.class);
		User user = org.mockito.Mockito.mock(User.class);
		when(doctor.getUser()).thenReturn(user);

		when(doctorsRepository.findById(1)).thenReturn(Optional.of(doctor));

		boolean result = doctorsService.deleteManually(1);

		assertTrue(result);
		verify(userRepository).delete(user);
		verify(doctorsRepository).delete(doctor);
	}

	@DisplayName("deleteManually: throw NotFoundException when doctor not found")
	@Test
	void deleteManually_ShouldThrow_WhenDoctorNotFound() {
		when(doctorsRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> doctorsService.deleteManually(1));
		assertEquals("doctor: Doctor Not Found", ex.getMessage());

		verify(doctorsRepository, never()).delete(any());
	}

	// ============= getMyAppointments ============
	@DisplayName("getMyAppointments: success when user and doctor exist")
	@Test
	void getMyAppointments_ShouldReturnPagedResult_WhenUserAndDoctorExist() {
		String username = "doctorUser";
		Pageable pageable = TestPageConstants.PAGEABLE_0_2;

		User user = org.mockito.Mockito.mock(User.class);
		Doctors doctor = org.mockito.Mockito.mock(Doctors.class);
		when(doctor.getId()).thenReturn(10);

		Appointments a1 = new Appointments();
		Appointments a2 = new Appointments();

		Page<Appointments> page = new PageImpl<>(List.of(a1, a2), pageable, TestPageConstants.DEFAULT_SIZE);

		when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
		when(doctorsRepository.findByUser(user)).thenReturn(Optional.of(doctor));
		when(appointmentsRepository.findByDoctors_Id(10, pageable)).thenReturn(page);
		when(appointmentsMapper.toDTO(any(Appointments.class))).thenReturn(new AppointmentsResponseDTO());

		PagedResult<AppointmentsResponseDTO> result = doctorsService.getMyAppointments(username, pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		verify(appointmentsRepository).findByDoctors_Id(10, pageable);
	}

	// ============= getMySchedules ============
	@DisplayName("getMySchedules: success when user and doctor exist")
	@Test
	void getMySchedules_ShouldReturnPagedResult_WhenUserAndDoctorExist() {
		String username = "doctorUser";
		Pageable pageable = TestPageConstants.PAGEABLE_0_2;

		User user = org.mockito.Mockito.mock(User.class);
		Doctors doctor = org.mockito.Mockito.mock(Doctors.class);
		when(doctor.getId()).thenReturn(10);

		Schedules s1 = org.mockito.Mockito.mock(Schedules.class);
		Schedules s2 = org.mockito.Mockito.mock(Schedules.class);

		Page<Schedules> page = new PageImpl<>(List.of(s1, s2), pageable, TestPageConstants.DEFAULT_SIZE);

		when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
		when(doctorsRepository.findByUser(user)).thenReturn(Optional.of(doctor));
		when(schedulesRepository.findByDoctorsId(10, pageable)).thenReturn(page);
		when(schedulesMapper.toDTO(any(Schedules.class))).thenReturn(new SchedulesReponseDTO());

		PagedResult<SchedulesReponseDTO> result = doctorsService.getMySchedules(username, pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		verify(schedulesRepository).findByDoctorsId(10, pageable);
	}

	// ============= delete (soft) ============
	@DisplayName("delete (soft): success when doctor exists")
	@Test
	void delete_ShouldSoftDelete_WhenDoctorExists() {
		Integer id = 1;
		Doctors doctor = org.mockito.Mockito.mock(Doctors.class);

		DoctorsDeleteRequestDTO dto = new DoctorsDeleteRequestDTO();
		dto.setIsActive(false);

		when(doctorsRepository.findById(id)).thenReturn(Optional.of(doctor));
		when(doctorsMapper.toDTOOther(doctor)).thenReturn(new DoctorSummaryResponseDTO());

		DoctorSummaryResponseDTO result = doctorsService.delete(id, dto);

		assertNotNull(result);
		verify(doctorsMapper).toDTOOther(doctor);
	}

	@DisplayName("delete (soft): throw NotFoundException when doctor not found")
	@Test
	void delete_ShouldThrow_WhenDoctorNotFound() {
		Integer id = 1;
		DoctorsDeleteRequestDTO dto = new DoctorsDeleteRequestDTO();
		dto.setIsActive(false);

		when(doctorsRepository.findById(id)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> doctorsService.delete(id, dto));
		assertEquals("doctor: Doctor Not Found", ex.getMessage());
	}
}


