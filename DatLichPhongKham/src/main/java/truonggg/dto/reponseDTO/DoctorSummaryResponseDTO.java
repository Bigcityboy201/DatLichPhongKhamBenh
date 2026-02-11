package truonggg.dto.reponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSummaryResponseDTO {
	private Integer id;
	private Integer userId;
	private String fullName;
	private String email;
	private String phone;
	private int experienceYears;
	private String description;
	private String imageUrl;
	private Boolean active;
	private Boolean isFeatured;
	private Integer departmentId;
	private String departmentName;
}
