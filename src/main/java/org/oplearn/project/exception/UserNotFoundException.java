package org.oplearn.project.exception;

import org.oplearn.project.exception.base.NotFoundException;

public class UserNotFoundException extends NotFoundException {
  public UserNotFoundException() {
    super("org.oplearn.project.exception.UserNotFoundException");
  }
}
