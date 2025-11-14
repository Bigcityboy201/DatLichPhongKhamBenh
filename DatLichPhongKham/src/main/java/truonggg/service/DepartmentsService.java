package truonggg.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.dto.requestDTO.DepartmentsRequestDTO;
import truonggg.dto.requestDTO.DepartmentsUpdateRequestDTO;
import truonggg.reponse.PagedResult;

public interface DepartmentsService {
	DepartmentsResponseDTO createDepartment(DepartmentsRequestDTO dto);

	List<DepartmentsResponseDTO> getAll();

	PagedResult<DepartmentsResponseDTO> getAllPaged(Pageable pageable);

	DepartmentsResponseDTO update(Integer id, DepartmentsUpdateRequestDTO dto);

	DepartmentsResponseDTO delete(Integer id, DepartmentsUpdateRequestDTO dto);

	boolean delete(Integer id);

}
