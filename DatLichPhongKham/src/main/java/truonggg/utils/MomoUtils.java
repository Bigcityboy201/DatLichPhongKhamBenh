package truonggg.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MomoUtils {
	
	@Value("${momo.partnerCode:}")
	private String partnerCode;
	
	@Value("${momo.accessKey:}")
	private String accessKey;
	
	@Value("${momo.secretKey:}")
	private String secretKey;
	
	@Value("${momo.apiUrl:https://test-payment.momo.vn/v2/gateway/api/create}")
	private String apiUrl;
	
	@Value("${momo.returnUrl:http://localhost:8080/api/payments/momo-return}")
	private String returnUrl;
	
	@Value("${momo.notifyUrl:http://localhost:8080/api/payments/momo-callback}")
	private String notifyUrl;
	
	/**
	 * Tạo requestId unique
	 */
	public String generateRequestId() {
		return String.valueOf(System.currentTimeMillis());
	}
	
	/**
	 * Tạo orderId unique
	 */
	public String generateOrderId(Integer appointmentId) {
		return "APPOINTMENT_" + appointmentId + "_" + System.currentTimeMillis();
	}
	
	/**
	 * Tạo chữ ký (signature) cho request
	 */
	public String createSignature(Map<String, String> params) {
		try {
			// Sắp xếp các tham số theo thứ tự alphabet
			List<String> keys = new ArrayList<>(params.keySet());
			Collections.sort(keys);
			
			// Tạo query string
			StringBuilder queryString = new StringBuilder();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = params.get(key);
				if (value != null && !value.isEmpty()) {
					queryString.append(key).append("=").append(value);
					if (it.hasNext()) {
						queryString.append("&");
					}
				}
			}
			
			// Tạo HMAC SHA256
			Mac hmacSha256 = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			hmacSha256.init(secretKeySpec);
			byte[] hash = hmacSha256.doFinal(queryString.toString().getBytes(StandardCharsets.UTF_8));
			
			// Convert to hex string
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			
			return hexString.toString();
		} catch (Exception e) {
			throw new RuntimeException("Error creating MoMo signature", e);
		}
	}
	
	/**
	 * Verify signature từ callback
	 */
	public boolean verifySignature(Map<String, String> params, String signature) {
		// Tạo bản sao và loại bỏ signature khỏi params để tính toán
		Map<String, String> paramsForSign = new java.util.HashMap<>(params);
		paramsForSign.remove("signature");
		String calculatedSignature = createSignature(paramsForSign);
		return calculatedSignature.equalsIgnoreCase(signature);
	}
	
	/**
	 * Tạo payment request data cho MoMo (trả về Map để convert sang JSON)
	 */
	public Map<String, String> createPaymentRequest(String orderId, Long amount, String orderInfo) {
		String requestId = generateRequestId();
		
		Map<String, String> params = new java.util.HashMap<>();
		params.put("partnerCode", partnerCode);
		params.put("accessKey", accessKey);
		params.put("requestId", requestId);
		params.put("amount", String.valueOf(amount));
		params.put("orderId", orderId);
		params.put("orderInfo", orderInfo);
		params.put("returnUrl", returnUrl);
		params.put("notifyUrl", notifyUrl);
		params.put("extraData", "");
		params.put("requestType", "captureMoMoWallet");
		
		String signature = createSignature(params);
		params.put("signature", signature);
		
		return params;
	}
	
	/**
	 * Tạo URL thanh toán MoMo (trả về API endpoint để frontend gọi)
	 */
	public String getPaymentApiUrl() {
		return apiUrl;
	}
	
	public String getPartnerCode() {
		return partnerCode;
	}
	
	public String getAccessKey() {
		return accessKey;
	}
	
	public String getSecretKey() {
		return secretKey;
	}
	
	public String getApiUrl() {
		return apiUrl;
	}
	
	public String getReturnUrl() {
		return returnUrl;
	}
	
	public String getNotifyUrl() {
		return notifyUrl;
	}
}

