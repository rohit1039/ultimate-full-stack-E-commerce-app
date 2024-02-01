package com.ecommerce.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

	@Value("${app.jwt.secret}")
	private String SECRET_KEY;

	private Map<String, String> map = new HashMap<>();

	public void validateToken(String token) {

		try {
			Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);

		}
		catch (Exception e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}

	public String extractUsername(String token) {

		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * @param <T>
	 * @param token
	 * @param claimsResolver
	 * @return
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * @param token
	 * @return
	 */
	private Claims extractAllClaims(String token) {

		return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
	}

}
