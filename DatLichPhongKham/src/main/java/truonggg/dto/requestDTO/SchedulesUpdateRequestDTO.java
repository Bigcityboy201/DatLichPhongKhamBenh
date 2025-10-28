package truonggg.dto.requestDTO;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

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
public class SchedulesUpdateRequestDTO {

	@NotNull(message = "Schedule ID is required")
	private Integer id;

	private DayOfWeek dayOfWeek;

	private LocalDateTime startAt;

	private LocalDateTime endAt;

	private Integer doctorId;
}
