package org.oplearn.project.exception.base;

import java.util.Map;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.BLANK_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.MessageException.DEFAULT_CODE_SERVER_ERROR;
import static org.oplearn.project.exception.base.StatusConstants.SERVER_ERROR;

public class InternalServerError extends BaseException {
  public InternalServerError(String code, String message, Map<String, String> params) {
    super(code, message, SERVER_ERROR, params);
  }

  public InternalServerError() {
    super(DEFAULT_CODE_SERVER_ERROR, BLANK_MESSAGE, SERVER_ERROR, null);
  }

  public InternalServerError(String message) {
    super(DEFAULT_CODE_SERVER_ERROR, message, SERVER_ERROR, null);
  }
}
