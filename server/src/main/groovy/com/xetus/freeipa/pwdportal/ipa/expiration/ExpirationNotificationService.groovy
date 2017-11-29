package com.xetus.freeipa.pwdportal.ipa.expiration;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xetus.freeipa.pwdportal.notification.EmailNotification;
import com.xetus.freeipa.pwdportal.notification.EmailNotificationBuilder;
import com.xetus.freeipa.pwdportal.notification.EmailNotificationService;
import com.xetus.freeipa.pwdportal.notification.NotificationsConfig

import groovy.time.TimeDuration
import groovy.transform.CompileStatic;
import groovy.util.logging.Slf4j

@Slf4j
@Component
@CompileStatic
public class ExpirationNotificationService {
  
  EmailNotificationBuilder builder;
  ExpirationAuditConfig config;
  EmailNotificationService emailService;
  
  @Autowired
  public ExpirationNotificationService(EmailNotificationBuilder builder,
                                       ExpirationAuditConfig config,
                                       EmailNotificationService emailService) {
    this.builder = builder;
    this.config = config;
    this.emailService = emailService;
  }
  
  public void sendSummary(ExpirationAuditResult result) {
    for (String recipient : config.summaryRecipients) {
      EmailNotification email = builder.build(recipient, config.expirationSummary) {
          NotificationsConfig emailConfig ->
            [
              "entries": result.entries.collect { new AuditSummaryEntry(it) },
              "warningStartDay": result.warningStartDay,
              "computeDate": emailConfig.dateFormat.format(result.evaluationDate),
              "webUri": emailConfig.webUri
            ] as Map
        }
       emailService.sendMessage(email);
    }
  }
  
  public void notifyOfExpiration(String recipient, 
                                 String name, 
                                 TimeDuration timeToExpiry) {
    EmailNotification email = builder.build(recipient, config.expirationNotification) { 
      NotificationsConfig emailConfig ->
          [
            "name": name,
            "expired": timeToExpiry.toMilliseconds() <= 0,
            "daysToExpiry": timeToExpiry.days,
            "webUri": emailConfig.webUri
          ] as Map
      }
     emailService.sendMessage(email);
  }
  
  public static class AuditSummaryEntry {
    private String name;
    private boolean expired;
    private int daysToExpiry;
    
    public AuditSummaryEntry(ExpirationAuditResultEntry entry) {
      this.name = entry.user.uid;
      this.expired = entry.timeToExpiry.toMilliseconds() <= 0;
      this.daysToExpiry = entry.timeToExpiry.days
    }
    
    public String getName() { return this.name; }
    public boolean isExpired() { return this.expired; }
    public int getDaysToExpiry() { return this.daysToExpiry; }
  }
}