package truonggg.service.IMPL;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import truonggg.service.SchedulesService;

@Service
@RequiredArgsConstructor
public class SchedulesServiceIMPL implements SchedulesService {
	private final SchedulesRepository schedulesRepository;

	private final DoctorsRepository doctorsRepository;

	private final SchedulesMapper schedulesMapper;

	@Override
	public List<SchedulesReponseDTO> getAll() {
		List<Schedules> schedules = this.schedulesRepository.findAll();
		return this.schedulesMapper.toDTOList(schedules);
	}

	@Override
	public PagedResult<SchedulesReponseDTO> getAllPaged(Pageable pageable) {
		Page<Schedules> schedulesPage = this.schedulesRepository.findAll(pageable);
		List<SchedulesReponseDTO> dtoList = schedulesPage.stream().map(schedulesMapper::toDTO)
				.collect(Collectors.toList());

		return PagedResult.<SchedulesReponseDTO>builder().content(dtoList)
				.totalElements((int) schedulesPage.getTotalElements()).totalPages(schedulesPage.getTotalPages())
				.currentPage(schedulesPage.getNumber()).pageSize(schedulesPage.getSize()).build();
	}

	@Override
	public List<SchedulesReponseDTO> getByDoctorId(Integer doctorId) {
		List<Schedules> schedules = this.schedulesRepository.findByDoctorsId(doctorId);
		return this.schedulesMapper.toDTOList(schedules);
	}

	@Override
	public SchedulesReponseDTO save(SchedulesRequestDTO dto) {
		Doctors doctors = this.doctorsRepository.findById(dto.getDoctorId())
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found!"));
		Schedules schedules = this.schedulesMapper.toModel(dto);
		schedules.setDoctors(doctors);
		return this.schedulesMapper.toDTO(schedulesRepository.save(schedules));
	}

	@Override
	public SchedulesReponseDTO update(Integer id, SchedulesUpdateRequestDTO dto) {
		Schedules foundSchedule = this.schedulesRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("schedule", "Schedule Not Found"));

		// Cập nhật dayOfWeek nếu có
		if (dto.getDayOfWeek() != null) {
			foundSchedule.setDayOfWeek(dto.getDayOfWeek());
		}

		// Cập nhật startAt nếu có
		if (dto.getStartAt() != null) {
			foundSchedule.setStartAt(dto.getStartAt());
		}

		// Cập nhật endAt nếu có
		if (dto.getEndAt() != null) {
			foundSchedule.setEndAt(dto.getEndAt());
		}

		// Cập nhật doctor nếu có
		if (dto.getDoctorId() != null) {
			Doctors doctors = this.doctorsRepository.findById(dto.getDoctorId())
					.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));
			foundSchedule.setDoctors(doctors);
		}

		return this.schedulesMapper.toDTO(this.schedulesRepository.save(foundSchedule));
	}

	@Override
	public SchedulesReponseDTO delete(Integer id, SchedulesUpdateRequestDTO dto) {
		Schedules foundSchedule = this.schedulesRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("schedule", "Schedule Not Found"));

		if (dto.getActive() != null) {
			foundSchedule.setIsActive(dto.getActive());
			this.schedulesRepository.save(foundSchedule);
		}
		return this.schedulesMapper.toDTO(foundSchedule);
	}

	@Transactional
	@Override
	public boolean delete(Integer id) {
		Schedules foundSchedule = this.schedulesRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("schedule", "Schedule Not Found"));

		this.schedulesRepository.delete(foundSchedule);
		return true;
	}
}
