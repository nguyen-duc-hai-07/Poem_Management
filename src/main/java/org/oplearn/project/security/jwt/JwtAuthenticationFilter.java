package org.oplearn.project.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.repository.redis.TokenRedisRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.AUTHORIZATION;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.ROLES_CLAIM;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.TYPE_TOKEN;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;
  private final TokenRedisRepository tokenRedisRepository;

  @Override
  protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
  ) throws ServletException, IOException {
    String header = request.getHeader(AUTHORIZATION);

    if (Objects.isNull(header) || !header.startsWith(TYPE_TOKEN)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(TYPE_TOKEN.length());
    try {
      Claims claims = jwtTokenProvider.parseClaims(token);

      if (!jwtTokenProvider.isAccessToken(claims)
            || tokenRedisRepository.isAccessTokenBlacklisted(claims.getId())) {
        log.debug("(doFilterInternal) token is not an access token or is blacklisted");
        SecurityContextHolder.clearContext();
        filterChain.doFilter(request, response);
        return;
      }

      List<SimpleGrantedAuthority> authorities = extractRoles(claims).stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .toList();

      var authentication = new UsernamePasswordAuthenticationToken(
            claims.getSubject(), null, authorities);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (JwtException | IllegalArgumentException ex) {
      log.warn("(doFilterInternal) invalid token: {}", ex.getMessage());
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }

  @SuppressWarnings("unchecked")
  private List<String> extractRoles(Claims claims) {
    Object roles = claims.get(ROLES_CLAIM);
    return roles instanceof List<?> ? (List<String>) roles : List.of();
  }
}
