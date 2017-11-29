package com.xetus.freeipa.pwdportal.ipa.change;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.xetus.freeipa.pwdportal.ipa.FreeIPAClientService
import com.xetus.freeipa.pwdportal.model.PwPortalUser
import com.xetus.oss.iris.FreeIPAClient
import com.xetus.oss.iris.model.RPCResponse

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
@Component
public class ChangeService {
  
  FreeIPAClientService clientService
  ChangeNotificationService emailService;
  
  @Autowired
  public ChangeService(FreeIPAClientService clientService,
                       ChangeNotificationService emailService) {
    this.clientService = clientService;
    this.emailService = emailService;
  }
  
  public PwPortalUser change(String user, String password, String newPassword) {
    log.trace "Attempting password change request..."
    FreeIPAClient client = clientService.resetPassword(user, password, newPassword)
    log.info "User ${user} successfully changed password"
    
    RPCResponse<List<PwPortalUser>> r = (RPCResponse<List<PwPortalUser>>) client
        .userFind([], [uid: user])
    
    List<PwPortalUser> results = r.getResult()
    if (!results || results.size() < 1) {
      return null;
    }
    
    PwPortalUser account = results[0]
    if (account.email) {
      log.trace "Located user ${account}'s email: $account.email"
      emailService.notify(account.email, user, new Date());
    } else {
      log.warn "Failed to send password change notification email " +
               "to user $user; no email was found"
    }
    return account;
  }
}
