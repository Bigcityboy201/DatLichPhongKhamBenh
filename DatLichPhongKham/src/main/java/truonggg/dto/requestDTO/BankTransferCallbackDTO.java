package truonggg.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO để nhận callback từ Google Apps Script hoặc Casso API khi có giao dịch
 * chuyển khoản mới
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankTransferCallbackDTO {

	private String content;

	private Double amount;

	private String fromAccount;

	private String fromName;

	private String transactionDate;

	private String bankTransactionId;
}

