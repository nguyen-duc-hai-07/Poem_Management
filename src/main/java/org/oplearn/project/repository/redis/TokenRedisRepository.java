package org.oplearn.project.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {
  private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh_token:";
  private static final String ACCESS_TOKEN_BLACKLIST_KEY_PREFIX = "auth:access_token_blacklist:";
  private static final String BLACKLIST_VALUE = "1";

  private final StringRedisTemplate redisTemplate;

  public void saveRefreshToken(String tokenId, Long userId, Duration ttl) {
    redisTemplate.opsForValue().set(REFRESH_TOKEN_KEY_PREFIX + tokenId, String.valueOf(userId), ttl);
  }

  public Optional<Long> findUserIdByRefreshToken(String tokenId) {
    String userId = redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY_PREFIX + tokenId);
    return Optional.ofNullable(userId).map(Long::valueOf);
  }

  public void deleteRefreshToken(String tokenId) {
    redisTemplate.delete(REFRESH_TOKEN_KEY_PREFIX + tokenId);
  }

  public void blacklistAccessToken(String tokenId, Duration ttl) {
    if (!ttl.isNegative() && !ttl.isZero()) {
      redisTemplate.opsForValue().set(ACCESS_TOKEN_BLACKLIST_KEY_PREFIX + tokenId, BLACKLIST_VALUE, ttl);
    }
  }

  public boolean isAccessTokenBlacklisted(String tokenId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_TOKEN_BLACKLIST_KEY_PREFIX + tokenId));
  }
}
