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
    @Mapping(target = "description", source = "description")
    @Mapping(target = "isActive", source = "isActive")
    RoleResponseDTO toDTO(Role role);
}