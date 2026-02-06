package truonggg.sercurity;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import truonggg.reponse.ErrorCode;
import truonggg.reponse.ErrorReponse;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
			throws IOException {

		if (response.isCommitted()) {
			return;
		}

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		ErrorReponse errorResponse = ErrorReponse.of("You do not have permission to access this resource",
				ErrorCode.FORBIDDEN, "security");

		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
