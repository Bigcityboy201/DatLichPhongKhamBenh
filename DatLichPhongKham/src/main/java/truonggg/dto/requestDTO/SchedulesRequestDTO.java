package truonggg.dto.requestDTO;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
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
public class SchedulesRequestDTO {
	@NotNull(message = "Ngày trong tuần không được để trống")
	private DayOfWeek dayOfWeek;

	@NotNull(message = "Thời gian bắt đầu không được để trống")
	@Future(message = "Thời gian bắt đầu phải là tương lai")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime startAt;

	@NotNull(message = "Thời gian kết thúc không được để trống")
	@Future(message = "Thời gian kết thúc phải là tương lai")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime endAt;

	private boolean isActive;

	@NotNull(message = "ID bác sĩ không được để trống")
	private Integer doctorId;

	// Validation tuỳ chọn: đảm bảo endTime sau startTime
	@AssertTrue(message = "Thời gian kết thúc phải sau thời gian bắt đầu")
	public boolean isEndTimeAfterStartTime() {
		if (startAt == null || endAt == null) {
			return true; // bỏ qua nếu null, đã validate @NotNull
		}
		return endAt.isAfter(startAt);
	}

}
