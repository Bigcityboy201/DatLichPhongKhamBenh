package truonggg.user.application;

import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;

public interface UserSelfService {

	UserResponseDTO updateProfile(String userName, UserUpdateRequestDTO dto);
}


