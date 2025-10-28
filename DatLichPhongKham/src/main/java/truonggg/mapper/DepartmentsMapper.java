package truonggg.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import truonggg.Model.Departments;
import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.dto.requestDTO.DepartmentsRequestDTO;

@Mapper(componentModel = "spring")
public interface DepartmentsMapper {

	Departments toEntity(DepartmentsRequestDTO dto);

	DepartmentsResponseDTO toResponse(Departments entity);

	default List<DepartmentsResponseDTO> toDTOList(List<Departments> listDepartments) {
		if (listDepartments == null || listDepartments.isEmpty()) {
			return List.of();
		}
		return listDepartments.stream().map(this::toResponse).collect(Collectors.toList());
	}
}
