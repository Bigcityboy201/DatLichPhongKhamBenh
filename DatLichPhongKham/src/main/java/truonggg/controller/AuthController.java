package truonggg.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.SignInResponse;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.SignInRequest;
import truonggg.dto.requestDTO.SignUpRequest;
import truonggg.reponse.SuccessReponse;
import truonggg.service.user.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth")
public class AuthController {

	private final AuthService authService;

	// 123
	@PostMapping(path = "/signUp")
	public SuccessReponse<UserResponseDTO> signUp(@RequestBody @Valid SignUpRequest request) {
		return SuccessReponse.of(this.authService.signUp(request));
	}

	@PostMapping("/logout")
	public SuccessReponse<Boolean> logout() {
		return SuccessReponse.of(true);
	}

	@PostMapping(path = "/signIn")
	public SuccessReponse<SignInResponse> signIn(@RequestBody @Valid SignInRequest request) {
		return SuccessReponse.of(this.authService.signIn(request));
	}
}