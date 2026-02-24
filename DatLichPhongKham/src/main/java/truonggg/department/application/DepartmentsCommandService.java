package truonggg.department.application;

import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.dto.requestDTO.DepartmentsRequestDTO;
import truonggg.dto.requestDTO.DepartmentsUpdateRequestDTO;

public interface DepartmentsCommandService {
	DepartmentsResponseDTO createDepartment(DepartmentsRequestDTO dto);

	DepartmentsResponseDTO update(Integer id, DepartmentsUpdateRequestDTO dto);

	DepartmentsResponseDTO delete(Integer id, DepartmentsUpdateRequestDTO dto);

	boolean delete(Integer id);
}


