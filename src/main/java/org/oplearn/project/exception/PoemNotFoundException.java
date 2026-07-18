package org.oplearn.project.exception;

import org.oplearn.project.exception.base.NotFoundException;

public class PoemNotFoundException extends NotFoundException {
  public PoemNotFoundException() {
    super("org.oplearn.project.exception.PoemNotFoundException");
  }
}
