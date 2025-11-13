package truonggg.dto.reponseDTO;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignInResponse {

	private String token;
	private Date expiredDate;
	private UserResponseDTO user;
}
