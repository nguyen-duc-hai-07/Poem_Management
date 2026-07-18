package org.oplearn.project.configuration;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Aspect
@Configuration
public class AopConfiguration {
  @Around("@annotation(org.oplearn.project.annotation.TrackTime)")
  public Object aroundTrackTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.currentTimeMillis();
    try {
      return joinPoint.proceed();
    } finally {
      log.info("(aroundTrackTime) {} took {} ms",
            joinPoint.getSignature().toShortString(),
            System.currentTimeMillis() - startTime);
    }
  }
}
