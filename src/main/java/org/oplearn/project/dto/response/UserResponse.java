package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
  private Long id;
  private String username;
  private String name;
  private String email;

  public static UserResponse from(User user) {
    return new UserResponse(
          user.getId(),
          user.getUsername(),
          user.getName(),
          user.getEmail()
    );
  }
}
