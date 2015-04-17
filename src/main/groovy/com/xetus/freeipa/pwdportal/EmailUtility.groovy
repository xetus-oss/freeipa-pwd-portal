package com.xetus.freeipa.pwdportal

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Properties

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.inject.Inject
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Slf4j
@CompileStatic
class EmailUtility {

  DateFormat df = new SimpleDateFormat()
  SiteConfig config

  EmailUtility(SiteConfig config) {
    this.config = config
    this.df = new SimpleDateFormat(config.dateFormat)
  }
    
  Session getSession(EmailConfig mconfig) {
    Properties props = new Properties()
    props.setProperty("mail.transport.protocol","smtp");
    props.setProperty("mail.smtp.host", mconfig.smtpHost)
    props.setProperty("mail.smtp.port", mconfig.smtpPort)
    props.setProperty("mail.smtp.from", mconfig.smtpFrom)
    
    if (mconfig.smtpPass == null) {
      return Session.getDefaultInstance(props)
    }
    
    props.setProperty("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true");
    return Session.getInstance(props,
      new javax.mail.Authenticator() {
        PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(
            mconfig.smtpUser ?: mconfig.smtpFrom, mconfig.smtpPass);
        }
      }
    )
  }
  
  void emailPasswordResetUrl(String email, 
                             PasswordResetRequest request, 
                             String link) {
    def engine = new groovy.text.SimpleTemplateEngine()
    def binding = [
      "generatedLink": link,
      "requestId": request.requestId,
      "name": request.name,
      "requestIp": request.requestIp,
      "requestDate": df.format(request.requestDate),
      "expirationDate": df.format(request.expirationDate) 
    ]
    
    def subjectTmpl = engine
      .createTemplate(config.passwordResetEmailConfig.subjectTemplate)
      .make(binding)
      
    def messageTmpl = engine
      .createTemplate(config.passwordResetEmailConfig.messageTemplate)
      .make(binding)
      
    config.passwordResetEmailConfig.merge(config.defaultEmailConfig)
    sendMessage(config.passwordResetEmailConfig,
                email,
                subjectTmpl?.toString(),
                messageTmpl?.toString())
  }

  void emailPasswordChangeNotification(String email, String name, Date date) {
    def engine = new groovy.text.SimpleTemplateEngine()
    def binding = [
      "name": name,
      "date": df.format(date)  
    ]
    
    def subjectTmpl = engine
      .createTemplate(config.passwordChangeEmailConfig.subjectTemplate)
      .make(binding)
      
    def messageTmpl = engine
      .createTemplate(config.passwordChangeEmailConfig.messageTemplate)
      .make(binding)
      
    config.passwordChangeEmailConfig.merge(config.defaultEmailConfig)
    sendMessage(config.passwordChangeEmailConfig,
                email,
                subjectTmpl?.toString(),
                messageTmpl?.toString())
  }
  
  void sendMessage(EmailConfig mconfig, String to, 
                  String subject, String message) {
    log.debug("Sending message:\n"
            + "to: $to,\n"
            + "from: $mconfig.smtpFrom,\n"
            + "subject: $subject,\n"
            + "message: $message\n")
    
    Session lSession = getSession(mconfig)
    MimeMessage msg = new MimeMessage(lSession)
    
    msg.setRecipients(MimeMessage.RecipientType.TO, new InternetAddress(to))
    msg.setFrom(new InternetAddress(mconfig.smtpFrom))
    msg.setSubject(subject);
    msg.setText(message)
    
    Transport.send(msg)
  }
}
