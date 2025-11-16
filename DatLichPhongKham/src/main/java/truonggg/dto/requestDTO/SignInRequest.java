package truonggg.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInRequest {
	@NotBlank(message = "Username must not be blank")
	private String userName;

	@NotBlank(message = "Password must not be blank")
	private String password;
}