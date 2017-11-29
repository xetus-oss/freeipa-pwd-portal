package com.xetus.freeipa.pwdportal.notification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

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
  
  @Autowired
  public EmailNotificationService(NotificationsConfig config) {
    this.config = config;
  }
  
  void sendMessage(EmailNotification email) {
    sendMessage(email.recipient, email.subject, email.message)
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
