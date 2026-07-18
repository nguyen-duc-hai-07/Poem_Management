package org.oplearn.project.exception;

import org.oplearn.project.exception.base.ConflictException;

public class EmailAlreadyExistedException extends ConflictException {
  public EmailAlreadyExistedException() {
    super("org.oplearn.project.exception.EmailAlreadyExistedException");
  }
}
