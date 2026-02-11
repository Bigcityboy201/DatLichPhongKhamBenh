package truonggg.strategy;

import java.security.Key;

public interface JwtKeyStrategy {
	Key getSigningKey();
}
