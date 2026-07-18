package org.oplearn.project.exception.base;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.BAD_REQUEST_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.BLANK_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.MessageException.DEFAULT_CODE_BAD_REQUEST;
import static org.oplearn.project.exception.base.StatusConstants.BAD_REQUEST;

public class BadRequestException extends BaseException {
  public BadRequestException() {
    super(DEFAULT_CODE_BAD_REQUEST, BAD_REQUEST_MESSAGE, BAD_REQUEST, null);
  }

  public BadRequestException(String code) {
    super(code, BLANK_MESSAGE, BAD_REQUEST, null);
  }
}
