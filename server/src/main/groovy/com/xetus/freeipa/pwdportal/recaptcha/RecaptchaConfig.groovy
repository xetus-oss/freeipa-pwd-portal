package com.xetus.freeipa.pwdportal.recaptcha;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import groovy.transform.CompileStatic;

@Component
@CompileStatic
@ConfigurationProperties("recaptcha")
public class RecaptchaConfig {
  /**
   * If true indicates recaptcha should be enabled for the whole site.
   */
  boolean enabled;
}
