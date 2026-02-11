package truonggg.dto.requestDTO;

import java.sql.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
	@Min(value = 0, message = "Experience years must be >= 0")
	@Max(value = 60, message = "Experience years is too large")
	private Integer experienceYears;

	@Size(max = 2000, message = "Description max 2000 characters")
	private String description;

	@Size(max = 500)
	private String imageUrl;

	private Boolean isFeatured;

	// Department field
	@Positive(message = "Department id must be positive")
	private Integer departmentId;

	// User fields
	@Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
	private String fullName;

	@Email(message = "Email should be valid")
	@Size(max = 100)
	private String email;

	@Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Phone number is invalid")
	private String phone;

	@Size(max = 255)
	private String address;

	private Date dateOfBirth;
	// private Boolean isActive;
}
