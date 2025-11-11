package truonggg.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import truonggg.Model.Departments;
import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.dto.requestDTO.DepartmentsRequestDTO;
import truonggg.dto.requestDTO.DepartmentsUpdateRequestDTO;

@Mapper(componentModel = "spring")
public interface DepartmentsMapper {
	Departments toEntity(DepartmentsRequestDTO dto);

	@Mapping(source = "active", target = "isActive")
	Departments toEntity(DepartmentsUpdateRequestDTO dto);

	@Mapping(source = "isActive", target = "active")
	DepartmentsResponseDTO toResponse(Departments entity);

	default List<DepartmentsResponseDTO> toDTOList(List<Departments> listDepartments) {
		if (listDepartments == null || listDepartments.isEmpty()) {
			return List.of();
		}
		return listDepartments.stream().map(this::toResponse).collect(Collectors.toList());
	}
}
