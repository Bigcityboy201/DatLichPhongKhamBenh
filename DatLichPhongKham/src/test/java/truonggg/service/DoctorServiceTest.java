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
import truonggg.Model.Appointments;
import truonggg.Model.Departments;
import truonggg.Model.Doctors;
import truonggg.Model.Schedules;
import truonggg.Model.User;
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.reponseDTO.DoctorSummaryResponseDTO;
import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.DoctorsDeleteRequestDTO;
import truonggg.dto.requestDTO.DoctorsRequestDTO;
import truonggg.mapper.AppointmentsMapper;
import truonggg.mapper.DoctorsMapper;
import truonggg.mapper.SchedulesMapper;
import truonggg.repo.AppointmentsRepository;
import truonggg.repo.DepartmentsRepository;
import truonggg.repo.DoctorsRepository;
import truonggg.repo.SchedulesRepository;
import truonggg.repo.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.doctor.impl.DoctorsServiceIMPL;

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

	// ============= createDoctor ============
	@DisplayName("createDoctor: success when user and department exist")
	@Test
	void createDoctor_ShouldReturnSummary_WhenUserAndDepartmentExist() {
		DoctorsRequestDTO dto = new DoctorsRequestDTO();
		dto.setUserId(1);
		dto.setDepartmentId(2);
		dto.setIsFeatured(null);

		User user = new User();
		user.setUserId(1);
		Departments dep = new Departments();
		dep.setId(2);

		Doctors doctorEntity = new Doctors();
		Doctors savedDoctor = new Doctors();

		DoctorSummaryResponseDTO responseDTO = new DoctorSummaryResponseDTO();

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(departmentsRepository.findById(2)).thenReturn(Optional.of(dep));
		when(doctorsMapper.toEntity(dto)).thenReturn(doctorEntity);
		when(doctorsRepository.save(any(Doctors.class))).thenReturn(savedDoctor);
		when(doctorsMapper.toDTOOther(savedDoctor)).thenReturn(responseDTO);

		DoctorSummaryResponseDTO result = doctorsService.createDoctor(dto);

		assertNotNull(result);
		verify(userRepository).findById(1);
		verify(departmentsRepository).findById(2);
		verify(doctorsRepository).save(any(Doctors.class));
	}

	@DisplayName("createDoctor: throw NotFoundException when user not found")
	@Test
	void createDoctor_ShouldThrow_WhenUserNotFound() {
		DoctorsRequestDTO dto = new DoctorsRequestDTO();
		dto.setUserId(1);
		dto.setDepartmentId(2);

		when(userRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> doctorsService.createDoctor(dto));
		assertEquals("user: User Not Found!", ex.getMessage());

		verify(departmentsRepository, never()).findById(any());
		verify(doctorsRepository, never()).save(any());
	}

	@DisplayName("createDoctor: throw NotFoundException when department not found")
	@Test
	void createDoctor_ShouldThrow_WhenDepartmentNotFound() {
		DoctorsRequestDTO dto = new DoctorsRequestDTO();
		dto.setUserId(1);
		dto.setDepartmentId(2);

		User user = new User();
		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(departmentsRepository.findById(2)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> doctorsService.createDoctor(dto));
		assertEquals("department: Department Not Found!", ex.getMessage());

		verify(doctorsRepository, never()).save(any());
	}

	// ============= getDoctorsByDepartmentPaged ============
	@DisplayName("getDoctorsByDepartmentPaged: success when department exists")
	@Test
	void getDoctorsByDepartmentPaged_ShouldReturnPagedResult_WhenDepartmentExists() {
		Integer depId = 1;
		Pageable pageable = TestPageConstants.PAGEABLE_0_2;

		Doctors d1 = new Doctors();
		Doctors d2 = new Doctors();

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
		Doctors doctor = new Doctors();
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
		Doctors doctor = new Doctors();
		doctor.setId(1);
		User user = new User();
		doctor.setUser(user);

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

		User user = new User();
		user.setUserName(username);

		Doctors doctor = new Doctors();
		doctor.setId(10);

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

		User user = new User();
		user.setUserName(username);

		Doctors doctor = new Doctors();
		doctor.setId(10);

		Schedules s1 = new Schedules();
		Schedules s2 = new Schedules();

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
		Doctors doctor = new Doctors();

		DoctorsDeleteRequestDTO dto = new DoctorsDeleteRequestDTO();
		dto.setIsActive(false);

		when(doctorsRepository.findById(id)).thenReturn(Optional.of(doctor));
		when(doctorsRepository.save(doctor)).thenReturn(doctor);
		when(doctorsMapper.toDTOOther(doctor)).thenReturn(new DoctorSummaryResponseDTO());

		DoctorSummaryResponseDTO result = doctorsService.delete(id, dto);

		assertNotNull(result);
		verify(doctorsRepository).save(doctor);
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


