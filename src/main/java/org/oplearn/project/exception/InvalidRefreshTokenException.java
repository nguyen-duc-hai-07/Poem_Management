package org.oplearn.project.exception;

import org.oplearn.project.exception.base.UnauthorizedException;

public class InvalidRefreshTokenException extends UnauthorizedException {
  public InvalidRefreshTokenException() {
    super("org.oplearn.project.exception.InvalidRefreshTokenException");
  }
}
