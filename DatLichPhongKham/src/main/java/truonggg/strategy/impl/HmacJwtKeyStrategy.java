package truonggg.strategy.impl;

import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import truonggg.strategy.JwtKeyStrategy;

@Component
public class HmacJwtKeyStrategy implements JwtKeyStrategy {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Override
	public Key getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}