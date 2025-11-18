package truonggg.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.requestDTO.AppointmentsRequestDTO;
import truonggg.dto.requestDTO.AppointmentsUpdateRequestDTO;
import truonggg.reponse.PagedResult;

public interface AppointmentsService {
	AppointmentsResponseDTO createAppointments(final AppointmentsRequestDTO dto);

	List<AppointmentsResponseDTO> getAll();

	PagedResult<AppointmentsResponseDTO> getAllPaged(Pageable pageable);

	AppointmentsResponseDTO findById(Integer id);

	AppointmentsResponseDTO update(Integer id, AppointmentsUpdateRequestDTO dto);

	AppointmentsResponseDTO delete(Integer id);

	boolean deleteManually(Integer id);

	PagedResult<AppointmentsResponseDTO> getAppointmentByUserPaged(Integer userId, Pageable pageable);

}
