package truonggg.service.appointment;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.reponse.PagedResult;

public interface AppointmentsQueryService {

	PagedResult<AppointmentsResponseDTO> getAllPaged(Pageable pageable);

	AppointmentsResponseDTO findById(Integer id);

	PagedResult<AppointmentsResponseDTO> getAppointmentByCurrentUser(String userName, Pageable pageable);
}


