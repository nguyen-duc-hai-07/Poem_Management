package org.oplearn.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "password")
public class LoginRequest {
  @NotBlank(message = "user.username.not_blank")
  private String username;

  @NotBlank(message = "user.password.not_blank")
  private String password;
}
