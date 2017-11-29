package com.xetus.freeipa.pwdportal.ipa.reset;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.xetus.freeipa.pwdportal.ipa.FreeIPAClientService
import com.xetus.freeipa.pwdportal.model.PwPortalUser
import com.xetus.freeipa.pwdportal.notification.EmailNotificationService;
import com.xetus.oss.iris.FreeIPAAuthenticationManager
import com.xetus.oss.iris.FreeIPAClient
import com.xetus.oss.iris.model.RPCResponse

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@Component
@CompileStatic
public class ResetService {
  FreeIPAClientService clientService;
  ResetRequestBuilder resetRequestBuilder;
  ResetRequestCacheService resetRequestCache;
  ResetNotificationService emailService;
  
  @Autowired
  public ResetService(FreeIPAClientService clientService,
                      ResetRequestBuilder resetRequestBuilder,
                      ResetRequestCacheService resetRequestCache,
                      ResetNotificationService emailService) {
    this.clientService = clientService;
    this.resetRequestBuilder = resetRequestBuilder;
    this.resetRequestCache = resetRequestCache;
    this.emailService = emailService;
  }
  
  public ResetRequest request(String user, String requestIp) {
    FreeIPAClient client = clientService.getKerberosClient();
    RPCResponse<List<PwPortalUser>> r = (RPCResponse<List<PwPortalUser>>) client
        .userFind([], [uid: user])
    
    List<PwPortalUser> results = r.result
    if (!results || results.size() < 1) {
      throw new IllegalArgumentException();
    }
     
    PwPortalUser foundUser = results[0]
    String email = foundUser.email
    if (!email) {
      log.warn("No email found for user: $user! Email is required for "
             + "password reset!")
      throw new RuntimeException("No email found for user");
    }
    
    def request = resetRequestBuilder.build(user, email, requestIp)
    resetRequestCache.addRequest(request)
    
    emailService.sendResetLink(request.email, request);
    log.info("Issued request link for $user that is valid "
           + "until ${request.getExpirationDate()}")
    return request;
  }
  
  public ResetRequest reset(ResetFulfillment fulfillment) {
    String id = fulfillment.resetId
    ResetRequest request = resetRequestCache.getRequest(id);
    if (!request || request.expirationDate <= new Date()) {
      log.info "Received request with expired or invalid id: $id"
      throw new InvalidPasswordResetIdException();
    }
    
    if (request.name != fulfillment.user) {
      log.info "Received request with incorrect user; expected" +
                "$request.name, but got $fulfillment.user";
      throw new IllegalArgumentException("Wrong user");
    }
    
    if (request.token != fulfillment.token) {
      log.info "Received request with invalid token for request: $id";
    }
    
    FreeIPAClient krbClient = clientService.getKerberosClient()
    def tmpPwd = fulfillment.token + fulfillment.newPassword
    
    krbClient.passwd(request.name, tmpPwd)
    clientService.resetPassword(request.name, tmpPwd, fulfillment.newPassword)
    
    log.info "Successfully reset password for user: $request.name"
    emailService.notifyOfchange(request.email, request.name, new Date());
    
    resetRequestCache.removeRequest(request)
    return request;
  }
}
