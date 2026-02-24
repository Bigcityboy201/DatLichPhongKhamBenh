package truonggg.role.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import truonggg.role.domain.model.Role;
import truonggg.dto.reponseDTO.RoleResponseDTO;
import truonggg.dto.requestDTO.RoleRequestDTO;
import truonggg.dto.requestDTO.RoleUpdateRequestDTO;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "roleId", source = "roleId")
    @Mapping(target = "roleName", source = "roleName")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    RoleResponseDTO toDTO(Role role);

    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toEntity(RoleRequestDTO dto);

    @Mapping(target = "roleName", source = "roleName")
    @Mapping(target = "Description", source = "description")
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toEntity(RoleUpdateRequestDTO dto);
}