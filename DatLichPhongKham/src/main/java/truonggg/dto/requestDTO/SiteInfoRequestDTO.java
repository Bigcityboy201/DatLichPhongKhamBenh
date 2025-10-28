package truonggg.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class SiteInfoRequestDTO {
	@NotBlank(message = "Khóa cấu hình không được để trống")
	@Size(min = 2, max = 100, message = "Khóa cấu hình phải từ 2-100 ký tự")
	@Pattern(regexp = "^[a-z0-9_]+$", message = "Khóa cấu hình chỉ được chứa chữ thường, số và dấu gạch dưới")
	private String infoKey;

	@NotBlank(message = "Giá trị không được để trống")
	@Size(max = 5000, message = "Giá trị không được vượt quá 5000 ký tự")
	private String value;
}
