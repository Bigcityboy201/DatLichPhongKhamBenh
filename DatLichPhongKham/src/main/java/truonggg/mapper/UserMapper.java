package truonggg.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import truonggg.Model.User;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.SignUpRequest;
import truonggg.dto.requestDTO.UserRequestDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
	User toEntity(UserRequestDTO dto);

	@Mapping(source = "role.roleName", target = "role")

	UserResponseDTO toDTO(User user);

	User toModel(final SignUpRequest dto);

	default List<UserResponseDTO> toDTOList(List<User> users) {
		if (users == null || users.isEmpty())
			return List.of();
		return users.stream().map(this::toDTO).collect(Collectors.toList());
	}

}
