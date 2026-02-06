package truonggg.service.user;

import truonggg.dto.reponseDTO.SignInResponse;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.SignInRequest;
import truonggg.dto.requestDTO.SignUpRequest;

public interface AuthService {
	UserResponseDTO signUp(SignUpRequest dto);

	SignInResponse signIn(SignInRequest dto);
}
