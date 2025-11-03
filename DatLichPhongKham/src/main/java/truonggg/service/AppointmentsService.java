package truonggg.service;

import java.util.List;

import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.requestDTO.AppointmentsRequestDTO;
import truonggg.dto.requestDTO.AppointmentsUpdateRequestDTO;

public interface AppointmentsService {
	AppointmentsResponseDTO createAppointments(final AppointmentsRequestDTO dto);

	List<AppointmentsResponseDTO> getAll();

	AppointmentsResponseDTO findById(Integer id);

	AppointmentsResponseDTO update(Integer id, AppointmentsUpdateRequestDTO dto);

	AppointmentsResponseDTO delete(Integer id);

	boolean deleteManually(Integer id);
}
