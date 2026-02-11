package truonggg.service.department;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.reponse.PagedResult;

public interface DepartmentsQueryService {

	PagedResult<DepartmentsResponseDTO> getAllPaged(Pageable pageable);

	DepartmentsResponseDTO findById(Integer id);

	PagedResult<DepartmentsResponseDTO> searchDepartments(String keyword, Pageable pageable);
}
