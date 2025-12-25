package truonggg.dto.reponseDTO;

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
public class QRCodeResponseDTO {
	
	private String qrCodeUrl; // URL của QR code image
	private Double amount; // Số tiền (2000 đồng mặc định)
	private String paymentMethod; // BANK_TRANSFER
	private String accountNumber; // Số tài khoản
	private String bankName; // Tên ngân hàng
	private String content; // Nội dung chuyển khoản (để verify)
	private String template; // Template của QR code (compact2, compact, qr_only)
}

