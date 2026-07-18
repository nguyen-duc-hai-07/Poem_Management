package org.oplearn.project.service;

import org.oplearn.project.dto.request.LoginRequest;
import org.oplearn.project.dto.response.TokenResponse;

public interface AuthService {
  TokenResponse login(LoginRequest request);

  TokenResponse refresh(String refreshToken);

  void logout(String refreshToken, String accessToken);
}
