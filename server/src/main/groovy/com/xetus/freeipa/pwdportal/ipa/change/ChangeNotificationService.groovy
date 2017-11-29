package com.xetus.freeipa.pwdportal.ipa.change;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component;

import com.xetus.freeipa.pwdportal.notification.EmailNotification;
import com.xetus.freeipa.pwdportal.notification.EmailNotificationBuilder;
import com.xetus.freeipa.pwdportal.notification.EmailNotificationService;
import com.xetus.freeipa.pwdportal.notification.NotificationsConfig

import groovy.transform.CompileStatic;

@CompileStatic
@Component
public class ChangeNotificationService {
  ChangeNotificationConfig config;
  EmailNotificationBuilder builder;
  EmailNotificationService emailService;
  
  @Autowired
  public ChangeNotificationService(EmailNotificationBuilder builder,
                                   ChangeNotificationConfig config,
                                   EmailNotificationService emailService) {
    this.builder = builder;
    this.config = config;
    this.emailService = emailService;
  }
  
  public void notify(String recipient, String name, Date date) {
    EmailNotification email = builder.build(recipient, config.changeNotification) { 
        NotificationsConfig emailConfig ->
          ["name": name, "date": emailConfig.dateFormat.format(date)] as Map
      }
     emailService.sendMessage(email);
  }
}
