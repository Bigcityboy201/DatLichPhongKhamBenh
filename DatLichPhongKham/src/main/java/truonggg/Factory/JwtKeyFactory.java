package truonggg.Factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import truonggg.strategy.JwtKeyStrategy;
import truonggg.strategy.impl.HmacJwtKeyStrategy;

@Component
@RequiredArgsConstructor
public class JwtKeyFactory {

	private final HmacJwtKeyStrategy hmacJwtKeyStrategy;

	@Value("${jwt.algorithm:HS256}")
	private String algorithm;

	public JwtKeyStrategy getStrategy() {
		return switch (algorithm) {
		case "HS256" -> hmacJwtKeyStrategy;
		default -> throw new IllegalArgumentException("Unsupported JWT algorithm: " + algorithm);
		};
	}
}