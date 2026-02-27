package truonggg.department.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import truonggg.department.domain.model.Departments;
import truonggg.dto.reponseDTO.DepartmentsResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DepartmentsMapper {
    @Mapping(source = "isActive", target = "active")
    DepartmentsResponseDTO toResponse(Departments entity);

    default List<DepartmentsResponseDTO> toDTOList(List<Departments> listDepartments) {
        if (listDepartments == null || listDepartments.isEmpty()) {
            return List.of();
        }
        return listDepartments.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
