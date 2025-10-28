package truonggg.dto.reponseDTO;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
	private Integer userId;
	private String fullName;
	private String email;
	private String phone;
	private String userName;
	private String address;
	private Date dateOfBirth;
	private Date createdAt;
	private boolean isActive;
	
	// Role duy nhất của user
	private String role;
}