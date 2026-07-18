package org.oplearn.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class UserRequest {
  @NotBlank(message = "user.username.not_blank")
  private String username;

  @NotBlank(message = "user.password.not_blank")
  @Size(min = 8, message = "user.password.min_length")
  private String password;

  private String name;

  @Email(message = "user.email.invalid")
  private String email;
}
