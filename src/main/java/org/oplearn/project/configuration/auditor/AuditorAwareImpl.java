package org.oplearn.project.configuration.auditor;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

import static org.oplearn.project.constants.OpLearnConstants.AuditorConstant.ANONYMOUS;
import static org.oplearn.project.constants.OpLearnConstants.AuditorConstant.SYSTEM;

public class AuditorAwareImpl implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (Objects.isNull(authentication)
          || !authentication.isAuthenticated()
          || ANONYMOUS.equals(authentication.getName())) {
      return Optional.of(SYSTEM);
    }
    return Optional.of(authentication.getName());
  }
}
