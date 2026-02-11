package truonggg.dto.requestDTO;

import java.time.LocalDateTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
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
public class AppointmentsRequestDTO {

	@NotNull(message = "Ngày hẹn không được bỏ trống")
	@FutureOrPresent(message = "Thời gian hẹn phải là hiện tại hoặc trong tương lai")
	private LocalDateTime appointmentDateTime;

	@Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
	private String note;

	@NotNull(message = "UserId không được bỏ trống")
	private Integer userId;

	// Có thể null nếu để EMPLOYEE gán bác sĩ sau
	private Integer doctorId;
}
