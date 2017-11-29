package com.xetus.freeipa.pwdportal.ipa.reset;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.xetus.freeipa.pwdportal.ipa.change.ChangeNotificationService
import com.xetus.freeipa.pwdportal.notification.EmailNotification;
import com.xetus.freeipa.pwdportal.notification.EmailNotificationBuilder;
import com.xetus.freeipa.pwdportal.notification.EmailNotificationService;
import com.xetus.freeipa.pwdportal.notification.NotificationsConfig;

import groovy.transform.CompileStatic

@Component
@CompileStatic
public class ResetNotificationService {

  ResetConfig config;
  EmailNotificationBuilder builder;
  ChangeNotificationService changeEmailService;
  EmailNotificationService emailService;
  
  @Autowired
  public ResetNotificationService(EmailNotificationBuilder builder,
                                  ResetConfig config,
                                  ChangeNotificationService changeEmailService,
                                  EmailNotificationService emailService) {
    this.builder = builder;
    this.config = config;
    this.changeEmailService = changeEmailService;
    this.emailService = emailService;
  }
  
  public void notifyOfchange(String recipient, String name, Date date) {
    changeEmailService.notify(recipient, name, date);
  }
  
  public void sendResetLink(String recipient, ResetRequest request) {
    EmailNotification email = builder.build(recipient, config.resetNotification) { 
        NotificationsConfig emailConfig ->
          [
            "resetLink": new ResetLinkBuilder(emailConfig.webUri).build(request),
            "requestId": request.resetId,
            "name": request.name,
            "requestIp": request.requestIp,
            "requestDate": emailConfig.dateFormat.format(request.requestDate),
            "resetToken": request.token,
            "expirationDate": emailConfig.dateFormat.format(request.expirationDate) 
          ] as Map
      }
     emailService.sendMessage(email);
  }
}
