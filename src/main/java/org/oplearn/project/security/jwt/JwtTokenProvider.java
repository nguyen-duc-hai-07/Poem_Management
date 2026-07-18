package org.oplearn.project.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.ROLES_CLAIM;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.TOKEN_TYPE_ACCESS;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.TOKEN_TYPE_CLAIM;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.TOKEN_TYPE_REFRESH;

@Component
public class JwtTokenProvider {
  private final SecretKey secretKey;
  private final long expirationMs;
  private final long refreshExpirationMs;

  public JwtTokenProvider(
        @Value("${security.jwt.secret}") String base64Secret,
        @Value("${security.jwt.expiration-ms}") long expirationMs,
        @Value("${security.jwt.refresh-expiration-ms}") long refreshExpirationMs
  ) {
    this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
    this.expirationMs = expirationMs;
    this.refreshExpirationMs = refreshExpirationMs;
  }

  public long getExpirationMs() {
    return expirationMs;
  }

  public long getRefreshExpirationMs() {
    return refreshExpirationMs;
  }

  public String generateAccessToken(String subject, List<String> roles) {
    return buildToken(subject, Map.of(ROLES_CLAIM, roles, TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS), expirationMs);
  }

  public String generateRefreshToken(String subject) {
    return buildToken(subject, Map.of(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH), refreshExpirationMs);
  }

  /**
   * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
   */
  public Claims parseClaims(String token) {
    return Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
  }

  public boolean isAccessToken(Claims claims) {
    return TOKEN_TYPE_ACCESS.equals(claims.get(TOKEN_TYPE_CLAIM));
  }

  public boolean isRefreshToken(Claims claims) {
    return TOKEN_TYPE_REFRESH.equals(claims.get(TOKEN_TYPE_CLAIM));
  }

  private String buildToken(String subject, Map<String, Object> claims, long ttlMs) {
    Date now = new Date();
    return Jwts.builder()
          .claims(claims)
          .id(UUID.randomUUID().toString())
          .subject(subject)
          .issuedAt(now)
          .expiration(new Date(now.getTime() + ttlMs))
          .signWith(secretKey)
          .compact();
  }
}
