package com.xetus.freeipa.pwdportal

import java.util.Properties

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.inject.Inject
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Slf4j
@CompileStatic
class EmailUtility {

  SiteConfig config

  EmailUtility(SiteConfig config) {
    this.config = config
  }
    
  Properties getProperties(EmailConfig mconfig) {
    Properties props = new Properties()
    props.setProperty("mail.transport.protocol","smtp");
    props.setProperty("mail.smtp.host", mconfig.smtpHost)
    props.setProperty("mail.smtp.port", mconfig.smtpPort)
    props.setProperty("mail.smtp.from", mconfig.fromAddress)
    
    if (mconfig.password != null) {
      props.setProperty("mail.smtp.password", mconfig.password)
    }
    
    props
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
      "requestDate": request.requestDate,
      "expirationDate": request.expirationDate  
    ]
    
    def subjectTmpl = engine
      .createTemplate(config.passwordResetEmailConfig.subjectTemplate)
      .make(binding)
      
    def messageTmpl = engine
      .createTemplate(config.passwordResetEmailConfig.messageTemplate)
      .make(binding)
      
    config.passwordResetEmailConfig.merge(config.defaultEmailConfig)
    sendMessage(getProperties(config.passwordResetEmailConfig),
                email,
                config.passwordResetEmailConfig.fromAddress,
                config.passwordResetEmailConfig.password,
                subjectTmpl?.toString(),
                messageTmpl?.toString())
  }

  void emailPasswordChangeNotification(String email, String name, Date date) {
    def engine = new groovy.text.SimpleTemplateEngine()
    def binding = [
      "name": name,
      "date": date  
    ]
    
    def subjectTmpl = engine
      .createTemplate(config.passwordChangeEmailConfig.subjectTemplate)
      .make(binding)
      
    def messageTmpl = engine
      .createTemplate(config.passwordChangeEmailConfig.messageTemplate)
      .make(binding)
      
    config.passwordChangeEmailConfig.merge(config.defaultEmailConfig)
    sendMessage(getProperties(config.passwordChangeEmailConfig),
                email,
                config.passwordChangeEmailConfig.fromAddress,
                config.passwordChangeEmailConfig.password,
                subjectTmpl?.toString(),
                messageTmpl?.toString())
  }
  
  void sendMessage(Properties props, String to, String from,
                   String password, String subject, String message) {
    log.debug("Sending message:\n"
            + "to: $to,\n"
            + "from: $from,\n"
            + "subject: $subject,\n"
            + "message: $message\n")
    
    Session lSession = Session.getDefaultInstance(props,null);
    MimeMessage msg = new MimeMessage(lSession);
    
    msg.setRecipients(MimeMessage.RecipientType.TO, new InternetAddress(to))
    msg.setFrom(new InternetAddress(from));
    msg.setSubject(subject);
    msg.setText(message)
    
    Transport transporter = lSession.getTransport("smtp");
    transporter.connect(null, from, password);
    transporter.send(msg);
  }
}
