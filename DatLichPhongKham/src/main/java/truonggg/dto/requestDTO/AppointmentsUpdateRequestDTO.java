package truonggg.dto.requestDTO;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;
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
public class AppointmentsUpdateRequestDTO {

	private LocalDateTime appointmentDateTime;

	@Size(max = 500, message = "Note cannot exceed 500 characters")
	private String note;

	private Integer userId;

	private Integer doctorId;

	private Appointments_Enum status;
}
