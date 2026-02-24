package truonggg.schedules.application;

import org.springframework.data.domain.Pageable;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.reponse.PagedResult;

import java.util.List;

public interface SchedulesQueryService {

    List<SchedulesReponseDTO> getAll();

    PagedResult<SchedulesReponseDTO> getAllPaged(Pageable pageable);

    PagedResult<SchedulesReponseDTO> getByDoctorId(Integer doctorId, Pageable pageable);
}
