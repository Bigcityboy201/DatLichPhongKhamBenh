package truonggg.dto.reponseDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import truonggg.Enum.Appointments_Enum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentsResponseDTO {

	private Integer id;
	private LocalDateTime appointmentDateTime;
	private Appointments_Enum status;
	private String note;

	// Thông tin User
	private Integer userId;
	private String userFullName;
	private String userEmail;
	private String userPhone;

	// Thông tin bác sĩ
	private Integer doctorId;
	private String doctorFullName;
	private String doctorDepartmentName;
	private String doctorImageUrl;
	private Integer doctorExperienceYears;
}
