package truonggg.dto.requestDTO;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
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

	private DayOfWeek dayOfWeek;

	@Future(message = "Thời gian bắt đầu phải là tương lai")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime startAt;

	@Future(message = "Thời gian kết thúc phải là tương lai")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime endAt;

	private Integer doctorId;

	private Boolean active;

	@AssertTrue(message = "Thời gian kết thúc phải sau thời gian bắt đầu")
	public boolean isEndTimeAfterStartTime() {
		if (startAt == null || endAt == null) {
			return true; // bỏ qua nếu null, đã validate @NotNull
		}
		return endAt.isAfter(startAt);
	}
}
