package truonggg.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import truonggg.Model.UserRoles;
import truonggg.dto.reponseDTO.UserRolesResponseDTO;
import truonggg.dto.requestDTO.UserRolesRequestDTO;
import truonggg.dto.requestDTO.UserRolesUpdateRequestDTO;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserRolesMapper {
	
	@Mapping(target = "userRoleId", source = "userRoleID")
	@Mapping(target = "userId", source = "user.userId")
	@Mapping(target = "userName", source = "user.fullName")
	@Mapping(target = "userEmail", source = "user.email")
	@Mapping(target = "roleId", source = "role.roleId")
	@Mapping(target = "roleName", source = "role.roleName")
	@Mapping(target = "isActive", ignore = true)
	UserRolesResponseDTO toDTO(UserRoles userRoles);

	@Mapping(target = "userRoleID", ignore = true)
	@Mapping(target = "isActive", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "role", ignore = true)
	UserRoles toEntity(UserRolesRequestDTO dto);

	@Mapping(target = "userRoleID", ignore = true)
	@Mapping(target = "isActive", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "role", ignore = true)
	UserRoles toEntity(UserRolesUpdateRequestDTO dto);
}
