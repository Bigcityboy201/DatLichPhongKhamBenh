package truonggg.service.doctor;

import java.util.List;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.DoctorSummaryResponseDTO;
import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.reponse.PagedResult;

public interface DoctorQueryService {

	List<DoctorSummaryResponseDTO> getAll(Boolean featured);

	PagedResult<DoctorSummaryResponseDTO> getAllPaged(Pageable pageable);

	PagedResult<DoctorSummaryResponseDTO> getDoctorsByDepartmentPaged(Integer departmentId, Pageable pageable);

	DoctorsReponseDTO findById(Integer id);

	PagedResult<DoctorSummaryResponseDTO> searchDoctors(String keyword, Pageable pageable);
}
