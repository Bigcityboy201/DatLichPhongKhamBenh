package truonggg.doctor.application;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.reponseDTO.DoctorSummaryResponseDTO;
import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.DoctorUpdateRequestDTO;
import truonggg.reponse.PagedResult;

public interface DoctorSelfService {

	DoctorsReponseDTO findByUserName(String userName);

	DoctorSummaryResponseDTO updateProfile(DoctorUpdateRequestDTO dto, String userName);

	PagedResult<AppointmentsResponseDTO> getMyAppointments(String userName, Pageable pageable);

	PagedResult<SchedulesReponseDTO> getMySchedules(String userName, Pageable pageable);
}


