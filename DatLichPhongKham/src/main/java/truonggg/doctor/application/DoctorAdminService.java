package truonggg.doctor.application;

import truonggg.dto.reponseDTO.DoctorSummaryResponseDTO;
import truonggg.dto.requestDTO.DoctorUpdateRequestDTO;
import truonggg.dto.requestDTO.DoctorsDeleteRequestDTO;
import truonggg.dto.requestDTO.DoctorsRequestDTO;

public interface DoctorAdminService {

	DoctorSummaryResponseDTO createDoctor(DoctorsRequestDTO dto);

	DoctorSummaryResponseDTO updateWithUser(Integer id, DoctorUpdateRequestDTO dto);

	DoctorSummaryResponseDTO delete(Integer id, DoctorsDeleteRequestDTO dto);

	boolean deleteManually(Integer id);
}


