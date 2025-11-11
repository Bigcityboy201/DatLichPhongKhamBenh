package truonggg.dto.requestDTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

	@NotBlank(message = "Họ và tên không được để trống")
	@Size(min = 2, max = 50, message = "Họ và tên phải từ 2 đến 50 ký tự")
	private String fullName;

	@NotBlank(message = "Email không được để trống")
	@Email(message = "Email không hợp lệ")
	@Column(nullable = false, unique = true)
	private String email;

	@NotBlank(message = "Số điện thoại không được để trống")
	@Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng 0")
	private String phone;

	@NotBlank(message = "Tên đăng nhập không được để trống")
	@Size(min = 4, max = 20, message = "Tên đăng nhập phải từ 4 đến 20 ký tự")
	@Column(nullable = false, unique = true)
	private String userName;

	@NotBlank(message = "Mật khẩu không được để trống")
	@Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
	private String password;
}
