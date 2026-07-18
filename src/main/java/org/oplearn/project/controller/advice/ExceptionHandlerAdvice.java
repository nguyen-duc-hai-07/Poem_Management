package org.oplearn.project.controller.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.Error;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.exception.base.BaseException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.DEFAULT_LANGUAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.LANGUAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.PERCENT;
import static org.oplearn.project.constants.OpLearnConstants.MessageException.DEFAULT_CODE_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerAdvice {
  private final MessageSource messageSource;

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ResponseGeneral<Error>> handleBaseException(
        BaseException ex,
        WebRequest webRequest
  ) {
    log.error("(handleBaseException) code: {}, status: {}", ex.getCode(), ex.getStatus());
    return ResponseEntity
          .status(ex.getStatus())
          .body(getError(ex.getStatus(), ex.getCode(), webRequest.getLocale(), ex.getParams()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseGeneral<Error>> handleValidationExceptions(
        MethodArgumentNotValidException exception,
        WebRequest webRequest
  ) {
    String errorMessage = exception.getBindingResult().getFieldErrors().stream()
          .map(fieldError -> fieldError.getDefaultMessage())
          .findFirst()
          .orElse(exception.getMessage());

    log.error("(handleValidationExceptions) message: {}", errorMessage);
    return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(getError(HttpStatus.BAD_REQUEST.value(), errorMessage, getLanguage(webRequest)));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ResponseGeneral<Error>> handleConstraintViolationExceptions(
        ConstraintViolationException exception,
        WebRequest webRequest
  ) {
    String errorMessage = exception.getConstraintViolations().stream()
          .map(constraintViolation -> constraintViolation.getMessage())
          .findFirst()
          .orElse(exception.getMessage());

    log.error("(handleConstraintViolationExceptions) message: {}", errorMessage);
    return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(getError(HttpStatus.BAD_REQUEST.value(), errorMessage, getLanguage(webRequest)));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseGeneral<Error>> handleUnexpectedException(
        Exception exception,
        WebRequest webRequest
  ) {
    log.error("(handleUnexpectedException) unexpected error", exception);
    return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(getError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                DEFAULT_CODE_SERVER_ERROR,
                getLanguage(webRequest)
          ));
  }

  private String getLanguage(WebRequest webRequest) {
    return Objects.nonNull(webRequest.getHeader(LANGUAGE)) ?
          webRequest.getHeader(LANGUAGE) : DEFAULT_LANGUAGE;
  }

  private ResponseGeneral<Error> getError(int status, String code, String language) {
    return ResponseGeneral.of(
          status,
          HttpStatus.valueOf(status).getReasonPhrase(),
          Error.of(code, getMessage(code, new Locale(language)))
    );
  }

  private ResponseGeneral<Error> getError(int status, String code, Locale locale, Map<String, String> params) {
    return ResponseGeneral.of(
          status,
          HttpStatus.valueOf(status).getReasonPhrase(),
          Error.of(code, getMessage(code, locale, params))
    );
  }

  private String getMessage(String code, Locale locale, Map<String, String> params) {
    var message = getMessage(code, locale);
    if (params != null && !params.isEmpty()) {
      for (var param : params.entrySet()) {
        message = message.replace(getMessageParamsKey(param.getKey()), param.getValue());
      }
    }
    return message;
  }

  private String getMessage(String code, Locale locale) {
    try {
      return messageSource.getMessage(code, null, locale);
    } catch (Exception ex) {
      return code;
    }
  }

  private String getMessageParamsKey(String key) {
    return PERCENT + key + PERCENT;
  }
}
