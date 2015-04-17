package com.xetus.freeipa.pwdportal

import groovy.transform.CompileStatic
import groovy.transform.ToString

@ToString
@CompileStatic
class EmailConfig {

  /**
   * The SMTP host from which to send the email.
   */
  String smtpHost
  
  /**
   * The port on the SMTP host through which SMTP traffic is
   * routed.
   */
  String smtpPort
  
  /**
   * The email address from which the email should be sent.
   */
  String smtpFrom
  
  /**
   * The username that should be used to authenticate with the SMTP
   * host. If neither this nor {@link #smtpPass} are specified, 
   * Authentication is not used. If smtpUser is not specified but
   * smtpPass is, {@link #smtpUser} will be attempted as the user
   * name.
   */
  String smtpUser
  
  /**
   * The smtpPass for the email address from which the email should
   * be sent. Authentication is not used if this is unspecified.
   */
  String smtpPass
  
  /**
   * The subject template to use for generating the email. This 
   * template will be passed to a {@link 
   * groovy.text.SimpleTemplateEngine}'s {@link 
   * groovy.text.SimpleTemplateEngine#createTemplate(String)} method.
   * The specific bindings available to the subject template depends
   * on the email.
   */
  String subjectTemplate
  
  /**
   * The message template to use for generating the email. This 
   * template will be passed to a {@link 
   * groovy.text.SimpleTemplateEngine}'s {@link 
   * groovy.text.SimpleTemplateEngine#createTemplate(String)} method.
   * The specific bindings available to the message template depends
   * on the email.
   */
  String messageTemplate
  
  /**
   * Merges any unconfigured values in this {@link EmailConfig} with
   * the passed {@link EmailConfig}. This is mostly a utility method
   * to facilitate merging typical shared configurations (such as {@link 
   * #smtpHost}, {@link #smtpPort}, {@link #smtpFrom}, and {@link 
   * #smtpPass}) into specific email message templates.
   *  
   * @param other
   * @return the {@link EmailConfig} object after merging the other's
   * values as applicable
   */
  EmailConfig merge(EmailConfig other) {
    smtpHost = (smtpHost) ?: other.smtpHost
    smtpPort = (smtpPort) ?: other.smtpPort
    smtpFrom = (smtpFrom) ?: other.smtpFrom
    smtpPass = (smtpPass) ?: other.smtpPass
    subjectTemplate = (subjectTemplate) ?: other.subjectTemplate
    messageTemplate = (messageTemplate) ?: other.messageTemplate
    this
  }

}
