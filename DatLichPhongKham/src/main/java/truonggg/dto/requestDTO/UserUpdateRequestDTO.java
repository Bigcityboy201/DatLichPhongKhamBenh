package truonggg.dto.requestDTO;

import java.sql.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDTO {

	@Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
	private String fullName;

	@Email(message = "Email format is invalid")
	private String email;

	@Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
	private String phone;

	private String address;

	private Date dateOfBirth;

	private Boolean isActive;
}
