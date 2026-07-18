package org.oplearn.project.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.ENCODING_UTF_8;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.MESSAGE_SOURCE;

@Configuration
public class MessageSourceConfiguration {

  @Bean
  public MessageSource messageSource() {
    var messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasename(MESSAGE_SOURCE);
    messageSource.setDefaultEncoding(ENCODING_UTF_8);
    return messageSource;
  }

}
