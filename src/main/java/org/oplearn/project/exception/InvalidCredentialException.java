package org.oplearn.project.exception;

import org.oplearn.project.exception.base.UnauthorizedException;

public class InvalidCredentialException extends UnauthorizedException {
  public InvalidCredentialException() {
    super("org.oplearn.project.exception.InvalidCredentialException");
  }
}
