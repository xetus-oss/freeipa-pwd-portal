package com.xetus.freeipa.pwdportal.notification;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import groovy.text.SimpleTemplateEngine
import groovy.text.TemplateEngine
import groovy.transform.CompileStatic;
import groovy.transform.stc.SimpleType
import groovy.util.logging.Slf4j
import groovy.transform.stc.ClosureParams

@Slf4j
@CompileStatic
@Component
public class EmailNotificationBuilder {
  private NotificationsConfig config;
  private TemplateEngine engine = new SimpleTemplateEngine();
  
  @Autowired
  public EmailNotificationBuilder(NotificationsConfig config) {
    this.config = config;
  }
  
  public EmailNotification build(String recipient,
                                 EmailNotificationConfig emailConfig,
                                 @ClosureParams(value = SimpleType.class, options = ["com.xetus.freeipa.pwdportal.notification.NotificationsConfig"])
                                   Closure<Map> bindingBuilder) {
     Map binding = bindingBuilder(config);
     String subject = engine.createTemplate(emailConfig.subjectTemplate)
                            .make(binding);
     String message = engine.createTemplate(emailConfig.messageTemplate)
                            .make(binding);
    return new EmailNotification(recipient, subject, message);
  }
}
