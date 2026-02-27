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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import truonggg.Exception.NotFoundException;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.SchedulesRequestDTO;
import truonggg.dto.requestDTO.SchedulesUpdateRequestDTO;
import truonggg.schedules.mapper.SchedulesMapper;
import truonggg.doctor.infrastructure.DoctorsRepository;
import truonggg.reponse.PagedResult;
import truonggg.schedules.application.impl.SchedulesCommandServiceImpl;
import truonggg.schedules.application.impl.SchedulesQueryServiceImpl;
import truonggg.schedules.infrastructure.SchedulesRepository;
import truonggg.schedules.domain.model.Schedules;
import truonggg.doctor.domain.model.Doctors;

@ExtendWith(MockitoExtension.class)
public class SchedulesServiceTest {

	@Mock
	private SchedulesRepository schedulesRepository;

	@Mock
	private DoctorsRepository doctorsRepository;

	@Mock
	private SchedulesMapper schedulesMapper;

	@InjectMocks
	private SchedulesCommandServiceImpl schedulesCommandService;

	@InjectMocks
	private SchedulesQueryServiceImpl schedulesQueryService;

	// ============= getAllPaged ============
	@DisplayName("getAllPaged: success")
	@Test
	void getAllPaged_ShouldReturnPagedResult() {
		Pageable pageable = PageRequest.of(0, 2);
		Schedules s1 = org.mockito.Mockito.mock(Schedules.class);
		Schedules s2 = org.mockito.Mockito.mock(Schedules.class);
		Page<Schedules> page = new PageImpl<>(List.of(s1, s2), pageable, 2);

		when(schedulesRepository.findAll(pageable)).thenReturn(page);
		when(schedulesMapper.toDTO(s1)).thenReturn(new SchedulesReponseDTO());
		when(schedulesMapper.toDTO(s2)).thenReturn(new SchedulesReponseDTO());

		PagedResult<SchedulesReponseDTO> result = schedulesQueryService.getAllPaged(pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		verify(schedulesRepository).findAll(pageable);
	}

	// ============= save ============
	@DisplayName("save: success when doctor exists")
	@Test
	void save_ShouldCreateSchedule_WhenDoctorExists() {
		SchedulesRequestDTO dto = new SchedulesRequestDTO();
		dto.setDoctorId(1);
		dto.setDayOfWeek(truonggg.Enum.DayOfWeek.MONDAY);
		dto.setStartAt(java.time.LocalDateTime.now().plusDays(1));
		dto.setEndAt(java.time.LocalDateTime.now().plusDays(1).plusHours(1));

		Doctors doctor = org.mockito.Mockito.mock(Doctors.class);
		Schedules schedule = org.mockito.Mockito.mock(Schedules.class);
		SchedulesReponseDTO responseDTO = new SchedulesReponseDTO();

		when(doctorsRepository.findById(1)).thenReturn(Optional.of(doctor));
		when(doctor.addSchedule(dto.getDayOfWeek(), dto.getStartAt(), dto.getEndAt())).thenReturn(schedule);
		when(doctorsRepository.save(doctor)).thenReturn(doctor);
		when(schedulesMapper.toDTO(schedule)).thenReturn(responseDTO);

		SchedulesReponseDTO result = schedulesCommandService.save(dto);

		assertNotNull(result);
		verify(doctorsRepository).findById(1);
		verify(doctorsRepository).save(doctor);
		verify(schedulesRepository, never()).save(any());
	}

	@DisplayName("save: throw NotFoundException when doctor not found")
	@Test
	void save_ShouldThrow_WhenDoctorNotFound() {
		SchedulesRequestDTO dto = new SchedulesRequestDTO();
		dto.setDoctorId(1);

		when(doctorsRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> schedulesCommandService.save(dto));
		assertEquals("doctor: Doctor Not Found!", ex.getMessage());

		verify(schedulesRepository, never()).save(any());
	}

	// ============= update ============
	@DisplayName("update: success when schedule exists")
	@Test
	void update_ShouldApplyChanges_WhenScheduleExists() {
		Integer id = 1;
		Schedules foundSchedule = org.mockito.Mockito.mock(Schedules.class);
		Schedules updatedSchedule = org.mockito.Mockito.mock(Schedules.class);
		Doctors doctor = org.mockito.Mockito.mock(Doctors.class);

		SchedulesUpdateRequestDTO dto = new SchedulesUpdateRequestDTO();
		dto.setDoctorId(10);
		dto.setDayOfWeek(truonggg.Enum.DayOfWeek.TUESDAY);
		dto.setStartAt(java.time.LocalDateTime.now().plusDays(2));
		dto.setEndAt(java.time.LocalDateTime.now().plusDays(2).plusHours(2));

		when(schedulesRepository.findById(id)).thenReturn(Optional.of(foundSchedule));
		when(doctorsRepository.findById(10)).thenReturn(Optional.of(doctor));
		when(doctor.updateSchedule(id, dto.getDayOfWeek(), dto.getStartAt(), dto.getEndAt())).thenReturn(updatedSchedule);
		when(doctorsRepository.save(doctor)).thenReturn(doctor);
		when(schedulesMapper.toDTO(updatedSchedule)).thenReturn(new SchedulesReponseDTO());

		SchedulesReponseDTO result = schedulesCommandService.update(id, dto);

		assertNotNull(result);
		verify(doctorsRepository).save(doctor);
		verify(schedulesRepository, never()).save(any());
	}

	@DisplayName("update: throw NotFoundException when schedule not found")
	@Test
	void update_ShouldThrow_WhenScheduleNotFound() {
		SchedulesUpdateRequestDTO dto = new SchedulesUpdateRequestDTO();
		when(schedulesRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> schedulesCommandService.update(1, dto));
		assertEquals("schedule: Schedule Not Found", ex.getMessage());
	}

	// ============= delete soft ============
	@DisplayName("delete (soft): success when schedule exists")
	@Test
	void deleteSoft_ShouldUpdateStatus_WhenExists() {
		Integer id = 1;
		Doctors doctor = Doctors.createDefault();
		Schedules schedule = Schedules.create(
				truonggg.Enum.DayOfWeek.MONDAY,
				java.time.LocalDateTime.now().plusDays(1),
				java.time.LocalDateTime.now().plusDays(1).plusHours(1),
				doctor
		);
		schedule.activate(); // để đi nhánh deactivate

		SchedulesUpdateRequestDTO dto = new SchedulesUpdateRequestDTO();
		dto.setActive(false);

		when(schedulesRepository.findById(id)).thenReturn(Optional.of(schedule));
		when(schedulesMapper.toDTO(schedule)).thenReturn(new SchedulesReponseDTO());

		SchedulesReponseDTO result = schedulesCommandService.delete(id, dto);

		assertNotNull(result);
		assertEquals(false, schedule.getIsActive());
		verify(schedulesRepository, never()).save(any());
	}

	@DisplayName("delete (soft): throw NotFoundException when schedule not found")
	@Test
	void deleteSoft_ShouldThrow_WhenNotFound() {
		SchedulesUpdateRequestDTO dto = new SchedulesUpdateRequestDTO();
		dto.setActive(false);

		when(schedulesRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> schedulesCommandService.delete(1, dto));
		assertEquals("schedule: Schedule Not Found", ex.getMessage());
	}

	// ============= delete hard ============
	@DisplayName("delete (hard): success when schedule exists")
	@Test
	void deleteHard_ShouldDelete_WhenExists() {
		Schedules schedule = org.mockito.Mockito.mock(Schedules.class);
		when(schedulesRepository.findById(1)).thenReturn(Optional.of(schedule));

		boolean result = schedulesCommandService.delete(1);

		assertTrue(result);
		verify(schedulesRepository).delete(schedule);
	}

	@DisplayName("delete (hard): throw NotFoundException when schedule not found")
	@Test
	void deleteHard_ShouldThrow_WhenNotFound() {
		when(schedulesRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> schedulesCommandService.delete(1));
		assertEquals("schedule: Schedule Not Found", ex.getMessage());

		verify(schedulesRepository, never()).delete(any());
	}
}


