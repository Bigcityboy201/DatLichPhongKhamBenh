package truonggg.service.IMPL;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.Model.Doctors;
import truonggg.Model.Schedules;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.SchedulesDeleteRequestDTO;
import truonggg.dto.requestDTO.SchedulesRequestDTO;
import truonggg.dto.requestDTO.SchedulesUpdateRequestDTO;
import truonggg.mapper.SchedulesMapper;
import truonggg.repo.DoctorsRepository;
import truonggg.repo.SchedulesRepository;
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
	public SchedulesReponseDTO update(SchedulesUpdateRequestDTO dto) {
		Schedules foundSchedule = this.schedulesRepository.findById(dto.getId())
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
	public boolean delete(SchedulesDeleteRequestDTO dto) {
		Schedules foundSchedule = this.schedulesRepository.findById(dto.getId())
				.orElseThrow(() -> new NotFoundException("schedule", "Schedule Not Found"));

		if (dto.getIsActive() != null) {
			foundSchedule.setIsActive(dto.getIsActive());
			this.schedulesRepository.save(foundSchedule);
		}
		return true;
	}

	@Override
	public boolean delete(Integer id) {
		Schedules foundSchedule = this.schedulesRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("schedule", "Schedule Not Found"));

		this.schedulesRepository.delete(foundSchedule);
		return true;
	}
}
