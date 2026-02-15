package truonggg.service.appointment;

import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.reponseDTO.CancelAppointmentResponse;
import truonggg.dto.requestDTO.AppointmentsRequestDTO;
import truonggg.dto.requestDTO.AppointmentsUpdateRequestDTO;

public interface AppointmentsCommandService {

	public AppointmentsResponseDTO createAppointments(AppointmentsRequestDTO dto, Integer currentUserId);

	AppointmentsResponseDTO update(Integer id, AppointmentsUpdateRequestDTO dto);

	AppointmentsResponseDTO delete(Integer id);

	boolean deleteManually(Integer id);

	CancelAppointmentResponse cancelByUser(Integer id, String userName);

	AppointmentsResponseDTO assignDoctor(Integer appointmentId, Integer doctorId);
}
