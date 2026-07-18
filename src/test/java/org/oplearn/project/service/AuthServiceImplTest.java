package org.oplearn.project.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oplearn.project.dto.request.LoginRequest;
import org.oplearn.project.dto.response.TokenResponse;
import org.oplearn.project.entity.User;
import org.oplearn.project.exception.InvalidCredentialException;
import org.oplearn.project.exception.InvalidRefreshTokenException;
import org.oplearn.project.repository.UserRepository;
import org.oplearn.project.repository.redis.TokenRedisRepository;
import org.oplearn.project.security.jwt.JwtTokenProvider;
import org.oplearn.project.service.impl.AuthServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
  @Mock
  private UserRepository userRepository;

  @Mock
  private TokenRedisRepository tokenRedisRepository;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private Claims refreshClaims;

  @InjectMocks
  private AuthServiceImpl service;

  private User user;

  @BeforeEach
  void setUp() {
    user = User.builder().username("tu.nguyen").password("encoded").role("USER").build();
    user.setId(1L);
  }

  private void mockIssueTokens() {
    when(jwtTokenProvider.generateAccessToken(anyString(), anyList())).thenReturn("access-jwt");
    when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-jwt");
    Claims newRefreshClaims = org.mockito.Mockito.mock(Claims.class);
    when(newRefreshClaims.getId()).thenReturn("new-jti");
    when(jwtTokenProvider.parseClaims("refresh-jwt")).thenReturn(newRefreshClaims);
    when(jwtTokenProvider.getRefreshExpirationMs()).thenReturn(604800000L);
    lenient().when(jwtTokenProvider.getExpirationMs()).thenReturn(3600000L);
  }

  @Test
  void login_shouldIssueJwtPairAndStoreRefreshJtiInRedis() {
    when(userRepository.findByUsernameAndIsDeletedFalse("tu.nguyen")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
    mockIssueTokens();

    TokenResponse response = service.login(new LoginRequest("tu.nguyen", "password123"));

    assertEquals("access-jwt", response.getAccessToken());
    assertEquals("refresh-jwt", response.getRefreshToken());
    verify(jwtTokenProvider).generateAccessToken("tu.nguyen", List.of("USER"));
    verify(tokenRedisRepository).saveRefreshToken(
          eq("new-jti"), eq(1L), eq(Duration.ofMillis(604800000L)));
  }

  @Test
  void login_shouldThrowUnauthorized_whenPasswordWrong() {
    when(userRepository.findByUsernameAndIsDeletedFalse("tu.nguyen")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

    assertThrows(InvalidCredentialException.class,
          () -> service.login(new LoginRequest("tu.nguyen", "wrong")));
    verify(tokenRedisRepository, never()).saveRefreshToken(anyString(), anyLong(), any());
  }

  @Test
  void login_shouldThrowUnauthorized_whenUserMissing() {
    when(userRepository.findByUsernameAndIsDeletedFalse("ghost")).thenReturn(Optional.empty());

    assertThrows(InvalidCredentialException.class,
          () -> service.login(new LoginRequest("ghost", "password123")));
  }

  @Test
  void refresh_shouldRotate_whenRefreshJwtValidAndJtiInRedis() {
    when(jwtTokenProvider.parseClaims("old-refresh-jwt")).thenReturn(refreshClaims);
    when(jwtTokenProvider.isRefreshToken(refreshClaims)).thenReturn(true);
    when(refreshClaims.getId()).thenReturn("old-jti");
    when(tokenRedisRepository.findUserIdByRefreshToken("old-jti")).thenReturn(Optional.of(1L));
    when(userRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(user));
    mockIssueTokens();

    TokenResponse response = service.refresh("old-refresh-jwt");

    assertEquals("access-jwt", response.getAccessToken());
    verify(tokenRedisRepository).deleteRefreshToken("old-jti");
    verify(tokenRedisRepository).saveRefreshToken(eq("new-jti"), eq(1L), any(Duration.class));
  }

  @Test
  void refresh_shouldThrowUnauthorized_whenJwtInvalidOrExpired() {
    when(jwtTokenProvider.parseClaims("bad-jwt")).thenThrow(new JwtException("expired"));

    assertThrows(InvalidRefreshTokenException.class, () -> service.refresh("bad-jwt"));
  }

  @Test
  void refresh_shouldThrowUnauthorized_whenAccessTokenUsedAsRefresh() {
    when(jwtTokenProvider.parseClaims("access-jwt")).thenReturn(refreshClaims);
    when(jwtTokenProvider.isRefreshToken(refreshClaims)).thenReturn(false);

    assertThrows(InvalidRefreshTokenException.class, () -> service.refresh("access-jwt"));
  }

  @Test
  void refresh_shouldThrowUnauthorized_whenJtiRevoked() {
    when(jwtTokenProvider.parseClaims("old-refresh-jwt")).thenReturn(refreshClaims);
    when(jwtTokenProvider.isRefreshToken(refreshClaims)).thenReturn(true);
    when(refreshClaims.getId()).thenReturn("revoked-jti");
    when(tokenRedisRepository.findUserIdByRefreshToken("revoked-jti")).thenReturn(Optional.empty());

    assertThrows(InvalidRefreshTokenException.class, () -> service.refresh("old-refresh-jwt"));
    verify(tokenRedisRepository, never()).saveRefreshToken(anyString(), anyLong(), any());
  }

  @Test
  void logout_shouldDeleteRefreshJtiAndBlacklistAccessJti() {
    Claims accessClaims = org.mockito.Mockito.mock(Claims.class);
    when(jwtTokenProvider.parseClaims("refresh-jwt")).thenReturn(refreshClaims);
    when(jwtTokenProvider.isRefreshToken(refreshClaims)).thenReturn(true);
    when(refreshClaims.getId()).thenReturn("refresh-jti");
    when(jwtTokenProvider.parseClaims("access-jwt")).thenReturn(accessClaims);
    when(accessClaims.getId()).thenReturn("access-jti");
    when(accessClaims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 60000));

    service.logout("refresh-jwt", "access-jwt");

    verify(tokenRedisRepository).deleteRefreshToken("refresh-jti");
    verify(tokenRedisRepository).blacklistAccessToken(eq("access-jti"), any(Duration.class));
  }

  @Test
  void logout_shouldStillBlacklistAccess_whenRefreshTokenInvalid() {
    Claims accessClaims = org.mockito.Mockito.mock(Claims.class);
    when(jwtTokenProvider.parseClaims("bad-refresh")).thenThrow(new JwtException("invalid"));
    when(jwtTokenProvider.parseClaims("access-jwt")).thenReturn(accessClaims);
    when(accessClaims.getId()).thenReturn("access-jti");
    when(accessClaims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 60000));

    service.logout("bad-refresh", "access-jwt");

    verify(tokenRedisRepository, never()).deleteRefreshToken(anyString());
    verify(tokenRedisRepository).blacklistAccessToken(eq("access-jti"), any(Duration.class));
  }
}
