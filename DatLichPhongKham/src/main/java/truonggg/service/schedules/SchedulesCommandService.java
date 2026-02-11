package truonggg.service.schedules;

import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.SchedulesRequestDTO;
import truonggg.dto.requestDTO.SchedulesUpdateRequestDTO;

public interface SchedulesCommandService {

	SchedulesReponseDTO save(final SchedulesRequestDTO dto);

	SchedulesReponseDTO update(Integer id, SchedulesUpdateRequestDTO dto);

	SchedulesReponseDTO delete(Integer id, SchedulesUpdateRequestDTO dto);

	boolean delete(Integer id);
}


