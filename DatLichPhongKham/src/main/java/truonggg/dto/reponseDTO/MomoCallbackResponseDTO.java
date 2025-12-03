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
public class MomoCallbackResponseDTO {
	
	private String partnerCode;
	private String orderId;
	private String requestId;
	private Long amount;
	private String orderInfo;
	private String orderType;
	private Long transId;
	private Integer resultCode;
	private String message;
	private String payType;
	private Long responseTime;
	private String extraData;
	private String signature;
}


