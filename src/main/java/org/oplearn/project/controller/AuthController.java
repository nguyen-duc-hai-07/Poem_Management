package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.LoginRequest;
import org.oplearn.project.dto.request.RefreshTokenRequest;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.dto.response.TokenResponse;
import org.oplearn.project.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.AUTHORIZATION;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.TYPE_TOKEN;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.SUCCESS_MESSAGE;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService service;

  @PostMapping("/login")
  public ResponseGeneral<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    log.info("(login) username: {}", request.getUsername());
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.login(request));
  }

  @PostMapping("/refresh")
  public ResponseGeneral<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    log.info("(refresh)");
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.refresh(request.getRefreshToken()));
  }

  @PostMapping("/logout")
  public ResponseGeneral<Void> logout(
        @Valid @RequestBody RefreshTokenRequest request,
        @RequestHeader(name = AUTHORIZATION, required = false) String authorizationHeader
  ) {
    log.info("(logout)");
    service.logout(request.getRefreshToken(), extractAccessToken(authorizationHeader));
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  private String extractAccessToken(String authorizationHeader) {
    if (Objects.isNull(authorizationHeader) || !authorizationHeader.startsWith(TYPE_TOKEN)) {
      return null;
    }
    return authorizationHeader.substring(TYPE_TOKEN.length());
  }
}
