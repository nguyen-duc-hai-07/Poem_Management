package org.oplearn.project.security.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.oplearn.project.dto.response.Error;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.ENCODING_UTF_8;

@Component
@RequiredArgsConstructor
public class UnAuthenticationCustomHandler implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  @Override
  public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
  ) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(ENCODING_UTF_8);

    ResponseGeneral<Error> body = ResponseGeneral.of(
          HttpStatus.UNAUTHORIZED.value(),
          HttpStatus.UNAUTHORIZED.getReasonPhrase(),
          Error.of("unauthenticated", "Authentication is required to access this resource")
    );
    objectMapper.writeValue(response.getWriter(), body);
  }
}
