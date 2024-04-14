package com.ecommerce.userservice.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class TokenHelper {

  @Value("${app.jwt.secret}")
  private String SECRET_KEY;

  @Value("${app.jwt.expiration-in-ms}")
  private Long JWT_EXPIRATION_TIME;

  /**
   * @param token
   * @return
   */
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

  /**
   * @param userDetails
   * @return
   */
  public String generateToken(UserDetails userDetails) {

    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userDetails.getUsername());
  }

  /**
   * @param claims
   * @param subject
   * @return
   */
  private String createToken(Map<String, Object> claims, String subject) {

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME))
        .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
        .compact();
  }

  /**
   * @param token
   * @param userDetails
   * @return
   */
  public Boolean validateToken(String token, UserDetails userDetails) {

    try {
      Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
