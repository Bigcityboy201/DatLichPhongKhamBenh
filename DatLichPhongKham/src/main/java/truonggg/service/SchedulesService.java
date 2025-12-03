package truonggg.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.SchedulesRequestDTO;
import truonggg.dto.requestDTO.SchedulesUpdateRequestDTO;
import truonggg.reponse.PagedResult;

public interface SchedulesService {

	List<SchedulesReponseDTO> getAll();

	PagedResult<SchedulesReponseDTO> getAllPaged(Pageable pageable);

	PagedResult<SchedulesReponseDTO> getByDoctorId(Integer doctorId, Pageable pageable);

	SchedulesReponseDTO save(final SchedulesRequestDTO dto);

	SchedulesReponseDTO update(Integer id, SchedulesUpdateRequestDTO dto);

	SchedulesReponseDTO delete(Integer id, SchedulesUpdateRequestDTO dto);

	boolean delete(Integer id);
}
