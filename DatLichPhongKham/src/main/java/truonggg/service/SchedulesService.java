package truonggg.service;

import java.util.List;

import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.SchedulesDeleteRequestDTO;
import truonggg.dto.requestDTO.SchedulesRequestDTO;
import truonggg.dto.requestDTO.SchedulesUpdateRequestDTO;

public interface SchedulesService {

	List<SchedulesReponseDTO> getAll();

	List<SchedulesReponseDTO> getByDoctorId(Integer doctorId);

	SchedulesReponseDTO save(final SchedulesRequestDTO dto);
	
	SchedulesReponseDTO update(SchedulesUpdateRequestDTO dto);
	
	boolean delete(SchedulesDeleteRequestDTO dto);
	
	boolean delete(Integer id);
}
