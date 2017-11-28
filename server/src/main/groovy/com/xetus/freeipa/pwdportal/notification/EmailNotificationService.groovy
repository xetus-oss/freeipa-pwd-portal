package com.xetus.freeipa.pwdportal.notification

import java.text.DateFormat
import java.util.Date

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.xetus.freeipa.pwdportal.ipa.reset.ResetRequest

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Slf4j
@Component
@CompileStatic
class EmailNotificationService {

  private NotificationsConfig config;
  private ResetLinkBuilder linkBuilder;
  private DateFormat df;
  
  @Autowired
  public EmailNotificationService(NotificationsConfig config) {
    this.config = config;
    this.linkBuilder = new ResetLinkBuilder(config.webUri);
  }
  
  DateFormat getDateFormat() {
    if (df == null) {
      df = config.getDateFormat();
    }
    return df;
  }
  
  String getPortalReference() {
    return config.webUri != null ? 
      config.webUri : "your friendly neighborhood FreeIPA Password Portal"  
  }
    
  void emailPasswordResetUrl(String email, 
                             ResetRequest request) {
    def engine = new groovy.text.SimpleTemplateEngine()
    def binding = [
      "resetLink": linkBuilder.build(request),
      "requestId": request.resetId,
      "name": request.name,
      "requestIp": request.requestIp,
      "requestDate": getDateFormat().format(request.requestDate),
      "resetToken": request.token,
      "expirationDate": getDateFormat().format(request.expirationDate) 
    ]
    
    def subjectTmpl = engine
      .createTemplate(config.passwordResetEmailConfig.subjectTemplate)
      .make(binding)
      
    def messageTmpl = engine
      .createTemplate(config.passwordResetEmailConfig.messageTemplate)
      .make(binding)
    
    sendMessage(email, subjectTmpl?.toString(), messageTmpl?.toString())
  }

  void emailPasswordChangeNotification(String email, String name, Date date) {
    def engine = new groovy.text.SimpleTemplateEngine()
    def binding = [
      "name": name,
      "date": getDateFormat().format(date)  
    ]
    
    def subjectTmpl = engine
      .createTemplate(config.passwordChangeEmailConfig.subjectTemplate)
      .make(binding)
      
    def messageTmpl = engine
      .createTemplate(config.passwordChangeEmailConfig.messageTemplate)
      .make(binding)
      
    sendMessage(email, subjectTmpl?.toString(), messageTmpl?.toString())
  }

  void sendMessage(String to, String subject, String message) {
    log.debug("Sending message:\n"
            + "to: $to,\n"
            + "from: $config.senderEmail,\n"
            + "subject: $subject,\n"
            + "message: $message\n")
    
    Properties props = new Properties()
    props.setProperty("mail.transport.protocol","smtp");
    props.setProperty("mail.smtp.host", config.smtpHost)
    props.setProperty("mail.smtp.port", "$config.smtpPort")
    props.setProperty("mail.smtp.user", config.senderName)
    props.setProperty("mail.smtp.from", config.senderEmail)

    Session lSession = Session.getDefaultInstance(props)
    MimeMessage msg = new MimeMessage(lSession)
    
    msg.setRecipients(MimeMessage.RecipientType.TO, new InternetAddress(to))
    msg.setFrom(new InternetAddress(config.senderEmail))
    msg.setSubject(subject);
    msg.setText(message)
    
    Transport.send(msg)
  }
}
