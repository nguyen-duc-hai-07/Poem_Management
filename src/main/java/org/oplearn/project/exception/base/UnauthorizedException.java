package org.oplearn.project.exception.base;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.BLANK_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.MessageException.DEFAULT_CODE_UNAUTHORIZED;
import static org.oplearn.project.exception.base.StatusConstants.UNAUTHORIZED;

public class UnauthorizedException extends BaseException {
  public UnauthorizedException() {
    super(DEFAULT_CODE_UNAUTHORIZED, BLANK_MESSAGE, UNAUTHORIZED, null);
  }

  public UnauthorizedException(String code) {
    super(code, BLANK_MESSAGE, UNAUTHORIZED, null);
  }
}
