package org.oplearn.project.security.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.oplearn.project.dto.response.Error;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.ENCODING_UTF_8;

@Component
@RequiredArgsConstructor
public class UnAuthorizationCustomHandler implements AccessDeniedHandler {
  private final ObjectMapper objectMapper;

  @Override
  public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
  ) throws IOException {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(ENCODING_UTF_8);

    ResponseGeneral<Error> body = ResponseGeneral.of(
          HttpStatus.FORBIDDEN.value(),
          HttpStatus.FORBIDDEN.getReasonPhrase(),
          Error.of("access_denied", "You do not have permission to access this resource")
    );
    objectMapper.writeValue(response.getWriter(), body);
  }
}
