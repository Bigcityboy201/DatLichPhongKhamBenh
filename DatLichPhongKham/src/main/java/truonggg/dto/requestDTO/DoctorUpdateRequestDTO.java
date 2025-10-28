package truonggg.dto.requestDTO;

import java.sql.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

	// Doctor fields
	@NotNull(message = "Doctor ID is required")
	private Integer id;

	private Integer experienceYears;
	private String description;
	private String imageUrl;
	private Boolean isFeatured;

	// Department field
	private Integer departmentId;

	// User fields
	@NotBlank(message = "Full name is required")
	private String fullName;

	@Email(message = "Email should be valid")
	private String email;

	private String phone;
	private String address;
	private Date dateOfBirth;
	private Boolean isActive;
}
