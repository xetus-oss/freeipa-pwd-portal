package com.xetus.freeipa.pwdportal.ipa.change;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.xetus.freeipa.pwdportal.notification.EmailNotificationConfig;

import groovy.transform.CompileStatic;

@CompileStatic
@Component
@ConfigurationProperties(prefix = "password.change")
public class ChangeNotificationConfig {
  /**
   * The configuration for the password change email that is sent to any
   * user whose password was changed through the password reset portal.
   * Available bindings for both the subject and message are: <ul>
   * 
   *  <li>$name: The user's LDAP name
   *  <li>$date: The date the password was changed
   * 
   * </ul>
   */
  @NotNull
  EmailNotificationConfig changeNotification;
}
