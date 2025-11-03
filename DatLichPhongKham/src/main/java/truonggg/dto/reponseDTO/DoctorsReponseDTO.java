package truonggg.dto.reponseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorsReponseDTO {
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
	private List<String> schedules;
	private List<String> specializations;
	private List<AppointmentsResponseDTO> appointments;
}
