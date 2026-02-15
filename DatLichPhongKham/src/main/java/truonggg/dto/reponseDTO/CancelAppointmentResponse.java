package truonggg.dto.reponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CancelAppointmentResponse {

	private AppointmentsResponseDTO appointment;
	private String message;

}
