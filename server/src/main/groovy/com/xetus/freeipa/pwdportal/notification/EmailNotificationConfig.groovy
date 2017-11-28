package com.xetus.freeipa.pwdportal.notification

import groovy.transform.CompileStatic

/**
 * A simple data class for an EmailNotificatonConfig
 */
@CompileStatic
public class EmailNotificationConfig {
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
}