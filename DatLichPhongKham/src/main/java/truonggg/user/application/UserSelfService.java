package truonggg.user.application;

import truonggg.dto.reponseDTO.UserResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import truonggg.dto.requestDTO.UserUpdateRequestDTO;

public interface UserSelfService {

	UserResponseDTO updateProfile(String userName, UserUpdateRequestDTO dto);

	/**
	 * Upload ảnh đại diện cho user hiện tại và cập nhật avatarUrl.
	 */
	UserResponseDTO uploadAvatar(String userName, MultipartFile file);
}


