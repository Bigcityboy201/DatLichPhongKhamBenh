package truonggg.service;

import java.util.List;

import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.dto.requestDTO.DepartmentsDeleteRequestDTO;
import truonggg.dto.requestDTO.DepartmentsRequestDTO;
import truonggg.dto.requestDTO.DepartmentsUpdateRequestDTO;

public interface DepartmentsService {
	DepartmentsResponseDTO createDepartment(DepartmentsRequestDTO dto);

	List<DepartmentsResponseDTO> getAll();

	DepartmentsResponseDTO update(DepartmentsUpdateRequestDTO dto);

	boolean delete(DepartmentsDeleteRequestDTO dto);

	boolean delete(Integer id);

}
