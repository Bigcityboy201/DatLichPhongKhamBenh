package truonggg.service.schedules;

import java.util.List;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.reponse.PagedResult;

public interface SchedulesQueryService {

	List<SchedulesReponseDTO> getAll();

	PagedResult<SchedulesReponseDTO> getAllPaged(Pageable pageable);

	PagedResult<SchedulesReponseDTO> getByDoctorId(Integer doctorId, Pageable pageable);
}


