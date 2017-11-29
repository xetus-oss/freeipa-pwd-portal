package com.xetus.freeipa.pwdportal.ipa.expiration;

import javax.validation.constraints.NotNull

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component

import com.xetus.freeipa.pwdportal.notification.EmailNotificationConfig

import groovy.transform.CompileStatic

@CompileStatic
@Component
@ConfigurationProperties(prefix = "password.expiration")
public class ExpirationAuditConfig {
  /**
   * The number of days prior to password expiration to start notifying
   * the user of pending password expiration
   */
  @NotNull
  Integer warningStartDay;
  
  /**
   * A listing of recipient UIDs who should receive summary emails 
   * detailing users with pending expirations
   * 
   * TODO: allow this to be users with a specific role, etc...
   */
  List<String> summaryRecipients = []
  
  /**
   * The configuration for the password expiration email that is sent to
   * any user whose password is expiring soon. Available bindings for
   * both the subject and the message are: <ul>
   *
   *  <li>$name: The user's LDAP name
   *  <li>$webUri: The configured webUri
   *  <li>$duration: The {@link TimeDuration} until the password expires
   *
   * </ul>
   */
  @NotNull
  EmailNotificationConfig expirationNotification;
  

  /**
   * The configuration for the password expiration summary email detailing
   * user password expirations. Available bindings for both the subject
   * and the message are: <ul>
   *
   *  <li>$auditResult: The PasswordExpirationAuditResult object
   *  <li>$computeDate: The formatted date when the audit result was
   *  computed
   *
   * </ul>
   */
  @NotNull
  EmailNotificationConfig expirationSummary;
}
