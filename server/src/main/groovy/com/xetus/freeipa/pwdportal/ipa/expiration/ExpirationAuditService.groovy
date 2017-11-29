package com.xetus.freeipa.pwdportal.ipa.expiration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component;

import com.xetus.oss.iris.FreeIPAClient
import com.xetus.oss.iris.model.RPCResponse; 
import com.xetus.freeipa.pwdportal.ipa.FreeIPAClientService;
import com.xetus.freeipa.pwdportal.model.PwPortalUser
import com.xetus.freeipa.pwdportal.notification.EmailNotificationService

import groovy.time.TimeDuration;
import groovy.transform.CompileStatic;
import groovy.util.logging.Slf4j

@Slf4j
@Component
@CompileStatic
public class ExpirationAuditService {

  private FreeIPAClientService clientService;
  private ExpirationAuditConfig config;
  private ExpirationNotificationService emailService;
  private ExpirationCalculator expirationCalculator = new ExpirationCalculator();
  
  @Autowired
  public ExpirationAuditService(FreeIPAClientService clientService,
                                        ExpirationAuditConfig config,
                                        ExpirationNotificationService emailService) {
    this.clientService = clientService;
    this.config = config;
    this.emailService = emailService;
  }
  
  public ExpirationAuditResult audit(String... uids) {
    return audit(clientService.getKerberosClient(), uids);
  }
  
  public ExpirationAuditResult audit(FreeIPAClient client,
                                             String... uids) {
    Date evaluationDate = new Date();
    def params = Arrays.asList(uids);
    RPCResponse<List<PwPortalUser>> result = 
      (RPCResponse<List<PwPortalUser>>) client.userFind(params, [all: "true"]);
    
    
    List<ExpirationAuditResultEntry> entries = []
    ((List<PwPortalUser>) result.result).forEach { PwPortalUser user ->
      TimeDuration timeToExpiry = expirationCalculator.evaluate(user); 
      if (timeToExpiry.days <= config.warningStartDay) {
        entries << new ExpirationAuditResultEntry(user, timeToExpiry);
      }
    };
    
    return new ExpirationAuditResult(
      entries, 
      config.warningStartDay,
      evaluationDate
    );
  }
  
  public void notify(ExpirationAuditResult result) {
    for (ExpirationAuditResultEntry entry : result.entries) {
      if (!entry.user.email) {
        log.warn "Can not send password expiration email for " +
                 "${entry.user.uid}; user doesn't have an email registered!"
        continue;
      }
      emailService.notifyOfExpiration(
        entry.user.email,
        entry.user.uid,
        entry.timeToExpiry
      );
    }
    emailService.sendSummary(result)
  }
  
  @Scheduled(cron = "\${password.expiration.cron}")
  public void performRecurringAudit() {
    log.info "Performing scheduled password expiration audit..."
    this.notify(this.audit());
    log.info "Completed password expiration audit"
  }
}
