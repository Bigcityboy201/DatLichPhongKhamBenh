package truonggg.reponse;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorReponse {
	private final OperationType operationType = OperationType.Failure;
	private String message;
	private ErrorCode code;
	private String domain;
	private Map<String, Object> details;
	@JsonFormat(timezone = "Asia/Saigon", pattern = "dd/MM/yyyy hh:mm:ss")
	@JsonProperty(value = "thời gian")
	private final Date timestamp = new Date();

	public static ErrorReponse of(final String message, final ErrorCode code) {
		return ErrorReponse.builder().message(message).code(code).build();
	}

	public static ErrorReponse of(final String message, final ErrorCode code, final String domain) {
		return ErrorReponse.builder().message(message).code(code).domain(domain).build();
	}

	public static ErrorReponse of(final String message, final ErrorCode code, final String domain,
			final Map<String, Object> details) {
		return ErrorReponse.builder().message(message).code(code).domain(domain).details(details).build();
	}
}
