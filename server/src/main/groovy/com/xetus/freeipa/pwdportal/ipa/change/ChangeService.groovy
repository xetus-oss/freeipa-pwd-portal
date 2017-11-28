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
  
  @Autowired
  public ChangeService(FreeIPAClientService clientService) {
    this.clientService = clientService
  }
  
  public PwPortalUser change(String user, String password, String newPassword) {
    log.trace "Attempting password change request..."
    FreeIPAClient client = clientService.resetPassword(user, password, newPassword)
    log.info "User ${user} successfully changed password"
    
    RPCResponse<List<PwPortalUser>> r = (RPCResponse<List<PwPortalUser>>) client
        .userFind([], [uid: user])
    
    List<PwPortalUser> results = r.getResult()
    if (results && results.size() > 0) {
      return results[0]
    }
    return null
  }
}
