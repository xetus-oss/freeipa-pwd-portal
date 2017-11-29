package com.xetus.freeipa.pwdportal.notification;

import java.util.Map;

import groovy.transform.CompileStatic;

@CompileStatic
public class EmailNotification {
  private String recipient;
  private String subject;
  private String message;
  
  public EmailNotification(String recipient,
                           String subject, 
                           String message) {
    this.recipient = recipient;
    this.subject = subject;
    this.message = message;
  }
  
  public String getRecipient() {
    return this.recipient;
  }
  
  public String getSubject() {
    return this.subject;
  }
  
  public String getMessage() {
    return this.message;
  }
}
