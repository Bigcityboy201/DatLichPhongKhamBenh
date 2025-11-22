package truonggg.controller;

import java.util.Date;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.Model.User;
import truonggg.dto.reponseDTO.SignInResponse;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.SignInRequest;
import truonggg.dto.requestDTO.SignUpRequest;
import truonggg.mapper.UserMapper;
import truonggg.repo.UserRepository;
import truonggg.reponse.SuccessReponse;
import truonggg.sercurity.CustomUserDetails;
import truonggg.service.UserService;
import truonggg.utils.JwtUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth")
public class AuthController {

	private final UserService userService;
	private final UserMapper userMapper;
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;
	private final UserRepository userRepository;

	@PostMapping(path = "/signUp")
	public SuccessReponse<UserResponseDTO> signUp(@RequestBody @Valid SignUpRequest request) {
		User user = this.userMapper.toModel(request);
//		log.info("{}", user.getUserName());
		return SuccessReponse.of(this.userService.signUp(user));
	}

	@PostMapping("/logout")
	public SuccessReponse<Boolean> logout() {
		return SuccessReponse.of(true);
	}

	@PostMapping(path = "/signIn")
	public SuccessReponse<SignInResponse> signIn(@RequestBody @Valid SignInRequest request) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword()));

			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			User user = userRepository.findByUserName(userDetails.getUsername())
					.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
			if (Boolean.TRUE.equals(user.getIsActive())) {
				throw new AuthenticationException("User account is inactive") {
				};
			}

			String accessToken = jwtUtils.generateToken(userDetails);
			Date expiredDate = jwtUtils.extractExpiration(accessToken);
			UserResponseDTO userDTO = userMapper.toDTO(user);

			return SuccessReponse
					.of(SignInResponse.builder().token(accessToken).expiredDate(expiredDate).user(userDTO).build());

		} catch (BadCredentialsException ex) {
			throw new AuthenticationException("Username or password is incorrect") {
			};
		} catch (DisabledException ex) {
			throw new AuthenticationException("User account is disabled") {
			};
		} catch (LockedException ex) {
			throw new AuthenticationException("User account is locked") {
			};
		}
	}

}