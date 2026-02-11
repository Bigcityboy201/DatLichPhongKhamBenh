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
import truonggg.Model.Doctors;
import truonggg.Model.Schedules;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.SchedulesRequestDTO;
import truonggg.dto.requestDTO.SchedulesUpdateRequestDTO;
import truonggg.mapper.SchedulesMapper;
import truonggg.repo.DoctorsRepository;
import truonggg.repo.SchedulesRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.schedules.impl.SchedulesServiceImpl;

@ExtendWith(MockitoExtension.class)
public class SchedulesServiceTest {

	@Mock
	private SchedulesRepository schedulesRepository;

	@Mock
	private DoctorsRepository doctorsRepository;

	@Mock
	private SchedulesMapper schedulesMapper;

	@InjectMocks
	private SchedulesServiceImpl schedulesService;

	// ============= getAllPaged ============
	@DisplayName("getAllPaged: success")
	@Test
	void getAllPaged_ShouldReturnPagedResult() {
		Pageable pageable = PageRequest.of(0, 2);
		Schedules s1 = new Schedules();
		Schedules s2 = new Schedules();
		Page<Schedules> page = new PageImpl<>(List.of(s1, s2), pageable, 2);

		when(schedulesRepository.findAll(pageable)).thenReturn(page);
		when(schedulesMapper.toDTO(s1)).thenReturn(new SchedulesReponseDTO());
		when(schedulesMapper.toDTO(s2)).thenReturn(new SchedulesReponseDTO());

		PagedResult<SchedulesReponseDTO> result = schedulesService.getAllPaged(pageable);

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

		Doctors doctor = new Doctors();
		Schedules schedule = new Schedules();
		Schedules saved = new Schedules();
		SchedulesReponseDTO responseDTO = new SchedulesReponseDTO();

		when(doctorsRepository.findById(1)).thenReturn(Optional.of(doctor));
		when(schedulesMapper.toModel(dto)).thenReturn(schedule);
		when(schedulesRepository.save(schedule)).thenReturn(saved);
		when(schedulesMapper.toDTO(saved)).thenReturn(responseDTO);

		SchedulesReponseDTO result = schedulesService.save(dto);

		assertNotNull(result);
		verify(doctorsRepository).findById(1);
		verify(schedulesRepository).save(schedule);
	}

	@DisplayName("save: throw NotFoundException when doctor not found")
	@Test
	void save_ShouldThrow_WhenDoctorNotFound() {
		SchedulesRequestDTO dto = new SchedulesRequestDTO();
		dto.setDoctorId(1);

		when(doctorsRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> schedulesService.save(dto));
		assertEquals("doctor: Doctor Not Found!", ex.getMessage());

		verify(schedulesRepository, never()).save(any());
	}

	// ============= update ============
	@DisplayName("update: success when schedule exists")
	@Test
	void update_ShouldApplyChanges_WhenScheduleExists() {
		Integer id = 1;
		Schedules schedule = new Schedules();

		SchedulesUpdateRequestDTO dto = new SchedulesUpdateRequestDTO();

		when(schedulesRepository.findById(id)).thenReturn(Optional.of(schedule));
		when(schedulesRepository.save(schedule)).thenReturn(schedule);
		when(schedulesMapper.toDTO(schedule)).thenReturn(new SchedulesReponseDTO());

		SchedulesReponseDTO result = schedulesService.update(id, dto);

		assertNotNull(result);
		verify(schedulesRepository).save(schedule);
	}

	@DisplayName("update: throw NotFoundException when schedule not found")
	@Test
	void update_ShouldThrow_WhenScheduleNotFound() {
		SchedulesUpdateRequestDTO dto = new SchedulesUpdateRequestDTO();
		when(schedulesRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> schedulesService.update(1, dto));
		assertEquals("schedule: Schedule Not Found", ex.getMessage());
	}

	// ============= delete soft ============
	@DisplayName("delete (soft): success when schedule exists")
	@Test
	void deleteSoft_ShouldUpdateStatus_WhenExists() {
		Integer id = 1;
		Schedules schedule = new Schedules();

		SchedulesUpdateRequestDTO dto = new SchedulesUpdateRequestDTO();
		dto.setActive(false);

		when(schedulesRepository.findById(id)).thenReturn(Optional.of(schedule));
		when(schedulesRepository.save(schedule)).thenReturn(schedule);
		when(schedulesMapper.toDTO(schedule)).thenReturn(new SchedulesReponseDTO());

		SchedulesReponseDTO result = schedulesService.delete(id, dto);

		assertNotNull(result);
		assertEquals(false, schedule.getIsActive());
		verify(schedulesRepository).save(schedule);
	}

	@DisplayName("delete (soft): throw NotFoundException when schedule not found")
	@Test
	void deleteSoft_ShouldThrow_WhenNotFound() {
		SchedulesUpdateRequestDTO dto = new SchedulesUpdateRequestDTO();
		dto.setActive(false);

		when(schedulesRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> schedulesService.delete(1, dto));
		assertEquals("schedule: Schedule Not Found", ex.getMessage());
	}

	// ============= delete hard ============
	@DisplayName("delete (hard): success when schedule exists")
	@Test
	void deleteHard_ShouldDelete_WhenExists() {
		Schedules schedule = new Schedules();
		when(schedulesRepository.findById(1)).thenReturn(Optional.of(schedule));

		boolean result = schedulesService.delete(1);

		assertTrue(result);
		verify(schedulesRepository).delete(schedule);
	}

	@DisplayName("delete (hard): throw NotFoundException when schedule not found")
	@Test
	void deleteHard_ShouldThrow_WhenNotFound() {
		when(schedulesRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> schedulesService.delete(1));
		assertEquals("schedule: Schedule Not Found", ex.getMessage());

		verify(schedulesRepository, never()).delete(any());
	}
}


