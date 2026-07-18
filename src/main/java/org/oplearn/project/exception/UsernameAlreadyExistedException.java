package org.oplearn.project.exception;

import org.oplearn.project.exception.base.ConflictException;

public class UsernameAlreadyExistedException extends ConflictException {
  public UsernameAlreadyExistedException() {
    super("org.oplearn.project.exception.UsernameAlreadyExistedException");
  }
}
