package truonggg.dto.requestDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class DoctorsRequestDTO {
	@NotNull(message = "Số năm kinh nghiệm không được bỏ trống")
	@Min(value = 0, message = "Số năm kinh nghiệm phải >= 0")
	private Integer experienceYears;

	@NotBlank(message = "Mô tả không được bỏ trống")
	@Size(max = 500, message = "Mô tả tối đa 500 ký tự")
	private String description;

	@NotNull(message = "UserId không được bỏ trống")
	private Integer userId;

	@NotNull(message = "DepartmentId không được bỏ trống")
	private Integer departmentId;

	private Boolean isFeatured;
}
