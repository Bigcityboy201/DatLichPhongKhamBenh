package truonggg.service;

import java.util.List;

import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.requestDTO.AppointmentsDeleteRequestDTO;
import truonggg.dto.requestDTO.AppointmentsRequestDTO;
import truonggg.dto.requestDTO.AppointmentsUpdateRequestDTO;

public interface AppointmentsService {
	AppointmentsResponseDTO createAppointments(final AppointmentsRequestDTO dto);

	List<AppointmentsResponseDTO> getAll();

	AppointmentsResponseDTO findById(Integer id);

	AppointmentsResponseDTO update(AppointmentsUpdateRequestDTO dto);

	boolean delete(AppointmentsDeleteRequestDTO dto);

	boolean delete(Integer id);
}
