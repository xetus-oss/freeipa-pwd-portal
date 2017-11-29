package com.xetus.freeipa.pwdportal;

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import groovy.transform.CompileStatic

@CompileStatic
@Component
@ConfigurationProperties(prefix = "pwd-portal")
public class PwdPortalConfig {
  /**
   * The key for the request header from which to extract the remote
   * user's IP if the password portal is being served from behind a
   * proxy.
   */
  String xForwardedForHeader = "X-Forwarded-For"
  
  /**
   * If true indicates recaptcha should be disabled for the whole site
   */
  boolean disableRecaptcha;
}
