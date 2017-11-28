package com.xetus.freeipa.pwdportal.notification;

import java.text.DateFormat
import java.text.SimpleDateFormat

import javax.validation.constraints.NotNull

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import groovy.transform.CompileStatic

/**
 * The configuration object that allows customization
 * of the various email notification templates used by
 * the FreeIPA Password Portal.
 */
@CompileStatic
@Component
@ConfigurationProperties(prefix = "notifications")
public class NotificationsConfig {

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
   * Note that dates are formatted according to the dateFormat configuration
   */
  @NotNull
  EmailNotificationConfig passwordResetEmailConfig

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
  EmailNotificationConfig passwordChangeEmailConfig
  
  /**
   * The optional web URI at which users access the FreeIPA password 
   * portal instance. This will be included in emails to redirect users
   * back to the portal during password resets.
   * 
   *  If not specified, the emails will use: "your friendly 
   *  neighborhood FreeIPA Password Portal".
   */
  @NotNull
  String webUri;

  /**
   * The SMTP host that can be used to send emails
   */
  @NotNull
  String smtpHost;

  /**
   * The port to which the SMTP service on the configured `smtpHost` is
   * exposed
   */
  @NotNull
  Integer smtpPort = 25;

  /**
   * The mane for the email account from which the emails are sent.
   *
   * e.g. "FreeIPA Password Portal"
   */
  String senderName = "FreeIPA Password Portal";

  /**
   * The email account from which the emails are sent.
   *
   * e.g. "noreply@example.org"
   */
  @NotNull
  String senderEmail;

  /**
   * The date format to use when sending emails. Should be a
   * pattern according to {@link java.text.SimpleDateFormat}
   */
  @NotNull
  String templateDateFormat = "hh:mm a zzz 'on' MMMM dd yyyy";

  public DateFormat getDateFormat() {
    return this.templateDateFormat != null ? 
        new SimpleDateFormat(this.templateDateFormat) :
        new SimpleDateFormat();
  }
}