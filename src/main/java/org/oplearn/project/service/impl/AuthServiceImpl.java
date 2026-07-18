package org.oplearn.project.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.LoginRequest;
import org.oplearn.project.dto.response.TokenResponse;
import org.oplearn.project.entity.User;
import org.oplearn.project.exception.InvalidCredentialException;
import org.oplearn.project.exception.InvalidRefreshTokenException;
import org.oplearn.project.repository.UserRepository;
import org.oplearn.project.repository.redis.TokenRedisRepository;
import org.oplearn.project.security.jwt.JwtTokenProvider;
import org.oplearn.project.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.TYPE_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final TokenRedisRepository tokenRedisRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final PasswordEncoder passwordEncoder;

  @Override
  public TokenResponse login(LoginRequest request) {
    User user = userRepository.findByUsernameAndIsDeletedFalse(request.getUsername())
          .orElseThrow(InvalidCredentialException::new);

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new InvalidCredentialException();
    }
    return issueTokens(user);
  }

  @Override
  public TokenResponse refresh(String refreshToken) {
    Claims claims = parseRefreshTokenClaims(refreshToken);

    Long userId = tokenRedisRepository.findUserIdByRefreshToken(claims.getId())
          .orElseThrow(InvalidRefreshTokenException::new);

    User user = userRepository.findByIdAndIsDeletedFalse(userId)
          .orElseThrow(InvalidRefreshTokenException::new);

    tokenRedisRepository.deleteRefreshToken(claims.getId());
    return issueTokens(user);
  }

  @Override
  public void logout(String refreshToken, String accessToken) {
    try {
      Claims claims = parseRefreshTokenClaims(refreshToken);
      tokenRedisRepository.deleteRefreshToken(claims.getId());
    } catch (InvalidRefreshTokenException ex) {
      log.warn("(logout) skip invalid refresh token");
    }

    if (StringUtils.hasText(accessToken)) {
      blacklistAccessToken(accessToken);
    }
  }

  private Claims parseRefreshTokenClaims(String refreshToken) {
    try {
      Claims claims = jwtTokenProvider.parseClaims(refreshToken);
      if (!jwtTokenProvider.isRefreshToken(claims) || !StringUtils.hasText(claims.getId())) {
        throw new InvalidRefreshTokenException();
      }
      return claims;
    } catch (JwtException | IllegalArgumentException ex) {
      log.warn("(parseRefreshTokenClaims) invalid refresh token: {}", ex.getMessage());
      throw new InvalidRefreshTokenException();
    }
  }

  private void blacklistAccessToken(String accessToken) {
    try {
      Claims claims = jwtTokenProvider.parseClaims(accessToken);
      Duration remainingTtl = Duration.between(Instant.now(), claims.getExpiration().toInstant());
      tokenRedisRepository.blacklistAccessToken(claims.getId(), remainingTtl);
    } catch (JwtException | IllegalArgumentException ex) {
      log.warn("(blacklistAccessToken) skip invalid access token: {}", ex.getMessage());
    }
  }

  private TokenResponse issueTokens(User user) {
    String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), List.of(user.getRole()));
    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

    String refreshTokenId = jwtTokenProvider.parseClaims(refreshToken).getId();
    tokenRedisRepository.saveRefreshToken(
          refreshTokenId,
          user.getId(),
          Duration.ofMillis(jwtTokenProvider.getRefreshExpirationMs())
    );

    return TokenResponse.of(
          accessToken,
          refreshToken,
          TYPE_TOKEN.trim(),
          jwtTokenProvider.getExpirationMs() / 1000
    );
  }
}
