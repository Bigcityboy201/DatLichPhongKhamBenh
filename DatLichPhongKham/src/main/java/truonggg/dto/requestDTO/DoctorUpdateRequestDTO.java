package truonggg.dto.requestDTO;

import java.sql.Date;

import jakarta.validation.constraints.Email;
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
public class DoctorUpdateRequestDTO {
	private Integer experienceYears;
	private String description;
	private String imageUrl;
	private Boolean isFeatured;

	// Department field
	private Integer departmentId;

	// User fields
	private String fullName;

	@Email(message = "Email should be valid")
	private String email;

	private String phone;
	private String address;
	private Date dateOfBirth;
	private Boolean isActive;
}
