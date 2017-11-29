package com.xetus.freeipa.pwdportal.ipa.reset;

import javax.validation.constraints.NotNull

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import com.xetus.freeipa.pwdportal.notification.EmailNotificationConfig

import groovy.transform.CompileStatic

@CompileStatic
@Component
@ConfigurationProperties(prefix = "password.reset")
public class ResetConfig {
  /**
   * The configuration for the password reset email that is sent to users 
   * who have requested a password reset. Available bindings for both the 
   * subject and message are: <ul>
   * 
   *  <li>$name: {@link PasswordResetRequest#getName()}
   *  <li>$requestId: {@link PasswordResetRequest#getRequestId()}
   *  <li>$requestIp: {@link PasswordResetRequest#getRequestIp()}
   *  <li>$requestDate: {@link PasswordResetRequest#getRequestDate()}
   *  <li>$expirationDate: {@link PasswordResetRequest#getExpirationDate()}
   *  <li>$generatedLink: The url generated according to {@link 
   *      FreeIPARestService#generateResetLink(PasswordResetRequest)} that the user
   *      will follow to reset their password
   *      
   * </ul>
   * 
   * Note that dates are formatted according to the NotificationsConfig 
   * dateFormat configuration
   */
  @NotNull
  EmailNotificationConfig resetNotification
  
  /**
   * The time limit in seconds to impose on password reset
   * requests
   */
  int passwordResetRequestTimeLimit = (15 * 60);
}
