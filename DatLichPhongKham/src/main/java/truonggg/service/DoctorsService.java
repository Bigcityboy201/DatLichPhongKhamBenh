package truonggg.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.dto.requestDTO.DoctorUpdateRequestDTO;
import truonggg.dto.requestDTO.DoctorsDeleteRequestDTO;
import truonggg.dto.requestDTO.DoctorsRequestDTO;
import truonggg.reponse.PagedResult;

public interface DoctorsService {

	List<DoctorsReponseDTO> getAll(Boolean featured);

	DoctorsReponseDTO createDoctor(final DoctorsRequestDTO dto);

	PagedResult<DoctorsReponseDTO> getDoctorsByDepartmentPaged(Integer departmentsId, Pageable pageable);

	PagedResult<DoctorsReponseDTO> getAllPaged(Pageable pageable);

	DoctorsReponseDTO findById(Integer id);

	DoctorsReponseDTO updateProfile(Integer id, DoctorUpdateRequestDTO dto);

	DoctorsReponseDTO updateWithUser(Integer id, DoctorUpdateRequestDTO dto);

	DoctorsReponseDTO delete(Integer id, DoctorsDeleteRequestDTO dto);

	boolean deleteManually(Integer id);

	PagedResult<DoctorsReponseDTO> searchDoctors(String keyword, Pageable pageable);
}
