package org.oplearn.project.exception.base;

import java.util.HashMap;
import java.util.Map;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.BLANK_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.NOT_FOUND_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.MessageException.DEFAULT_CODE_NOTFOUND;
import static org.oplearn.project.exception.base.StatusConstants.NOT_FOUND;

public class NotFoundException extends BaseException {

  public NotFoundException(String id, String objectName) {
    super(DEFAULT_CODE_NOTFOUND, NOT_FOUND_MESSAGE, NOT_FOUND, createParams(id, objectName));
  }

  public NotFoundException() {
    super(DEFAULT_CODE_NOTFOUND, NOT_FOUND_MESSAGE, NOT_FOUND, null);
  }

  public NotFoundException(String code) {
    super(code, BLANK_MESSAGE, NOT_FOUND, null);
  }

  private static Map<String, String> createParams(String id, String objectName) {
    Map<String, String> params = new HashMap<>();
    params.put("id", id);
    params.put("objectName", objectName);
    return params;
  }
}
