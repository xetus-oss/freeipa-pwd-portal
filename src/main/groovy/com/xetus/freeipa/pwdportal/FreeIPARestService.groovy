package com.xetus.freeipa.pwdportal

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.security.SecureRandom

import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.FormParam
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces

import org.apache.cxf.message.Message
import org.apache.cxf.phase.PhaseInterceptorChain
import org.apache.cxf.transport.http.AbstractHTTPDestination

import com.xetus.freeipa.pwdportal.model.PwPortalUser
import com.xetus.freeipa.pwdportal.recaptcha.RecaptchaClient
import com.xetus.freeipa.pwdportal.recaptcha.RecaptchaResponse

import com.xetus.iris.FreeIPAAuthenticationManager
import com.xetus.iris.FreeIPAClient
import com.xetus.iris.InvalidPasswordException
import com.xetus.iris.InvalidUserOrRealmException
import com.xetus.iris.PasswordExpiredException
import com.xetus.iris.PasswordPolicyViolationException
import com.xetus.iris.model.RPCResponse

/**
 * The JAX-RS endpoints. Currently contains the following endpoints:<ul>
 * 
 * <li>{@link #resetRequest(String, String)} to request a password reset link
 * <li>{@link #reset(String, String)} to fulfill a password reset link
 * <li>{@link #changepassword(String, String, String, String)} to change an
 * existing password
 * 
 * </ul>
 * 
 * The {@link #resetRequest(String, String)} and {@link #reset(String, String)}
 * endpoints both require that the JVM has access to a valid JAAS 
 * configuration and that the JAAS configuration indicates a valid Kerberos 
 * configuration file and a FreeIPA host configured with adequate permissions 
 * to both retrieve a valid ticket from the KDC and change the user's 
 * password. Please see {@link SiteConfig} and the Readme for more details.
 */
@Path("/freeipa")
@Slf4j
@CompileStatic
class FreeIPARestService {
  
  public static final String RESET_REQUEST_ID_PARAM = "rid"
  
  public static final String CAPTCHA_FAILED_ERROR = "invalid-captcha-response"
  public static final String POLICY_VIOLATION_ERROR = "password-policy-violation"
  public static final String PASSWORD_EXPIRED_ERROR = "password-expired"
  public static final String INVALID_CREDENTIALS_ERROR = "invalid-credentials"
  public static final String UNKNOWN_EXCEPTION_ERROR = "unknown-exception" 
  public static final String NO_EMAIL_ERROR = "no-email-found"
  public static final String INVALID_RESET_REQUEST_ID = "invalid-reset-request-id"
  
  @Inject
  SiteConfig config
  
  PasswordResetRequestCache cache = PasswordResetRequestCache.getInstance()
  private SecureRandom random = new SecureRandom();
  private FreeIPAAuthenticationManager mgr
  private RecaptchaClient recaptcha
  private EmailUtility smtp
  
  @PostConstruct
  void initialize() {
    this.mgr = new FreeIPAAuthenticationManager(config.freeipaConfig)
    this.smtp = new EmailUtility(config)
    if (config.isRecaptchaEnabled()) {
      this.recaptcha = new RecaptchaClient(config.recaptchaPrivateKey)
    }
  }
  
  /**
   * Utility method to generate a random request ID
   * @return the random request ID that was generated
   */
  String generateRandomRid() {
    return new BigInteger(130, random).toString(32);
  }
  
  /**
   * Utility method to retrieve the HttpServletRequest
   * @return
   */
  HttpServletRequest getRequest() {
    Message message = PhaseInterceptorChain.getCurrentMessage()
    HttpServletRequest request = (HttpServletRequest) message
      .get(AbstractHTTPDestination.HTTP_REQUEST)
  }
  
  String getRemoteIp() {
    def request = getRequest()
    
    String remoteIp = request.getRemoteAddr()
    String xForwardedForValue = request.getHeader(config.xForwardedForHeader)
    if (xForwardedForValue) {
        remoteIp = xForwardedForValue.split(',')[0]?.trim()
    }
    
    return remoteIp
  }
  
  /**
   * Returns a boolean indicating whether the passed ReCaptcha response 
   * is valid.
   * @param response the response to a ReCaptcha challenge
   * @return boolean indicator of the validity of the response
   */
  boolean verifyRecaptcha(String response) {
    if (!config.isRecaptchaEnabled()) {
      return true
    }
    
    if (!response || response.empty) {
      return false
    } 
    
    RecaptchaResponse reCaptchaResponse = null
    try {
      reCaptchaResponse = recaptcha.verify(response, getRemoteIp())
    } catch(e) {
      log.error "Exception raised while consuming verification API", e
      return false
    }
        
    if (!reCaptchaResponse.isValid()) {
      log.warn "User failed recaptcha: $reCaptchaResponse"
    }
    
    return reCaptchaResponse.isValid()
  }
  
  /**
   * Generates a reset link from the passed reset request. The 
   * reset link will use the request servlet's target server
   * path parts in conjunction with the passed {@link 
   * PasswordResetLink#requestId} to generate the link
   * @param resetRequest the reset request for which to generate
   * a reset link
   * @return the generated link
   */
  String generateResetLink(PasswordResetRequest resetRequest) {
    def request = getRequest()
    def link = "${request.getScheme()}://${request.getServerName()}"
    def port = request.getServerPort()
    
    if (port != 80 && port != 443) {
      link += ":$port"
    }
    
    if (!request.getContextPath().empty) {
      link += request.getContextPath()
    }
    
    link += "?$RESET_REQUEST_ID_PARAM=${resetRequest.requestId}"
    log.debug "generated reset link: $link"
    link
  }

  /**
   * Changes the user's password to the newPass, assuming the recatpcha
   * response and credentials supplied are valid. On successful password
   * change, an email is sent to the email matching the supplied username
   * in the LDAP database indicating the user's password was changed 
   * (according to the {@link SiteConfig#getPasswordChangeEmailConfig()}.
   * 
   * This endpoint logs all received change attempts and their subsequent 
   * success (if applicable) at INFO level.
   *  
   * @param username
   * @param password
   * @param newPass
   * @param response
   * @return
   */
  @Path("/change")
  @Produces("application/json")
  @POST
  Response changepassword(@FormParam("user") String username, 
                         @FormParam("pass") String password,
                         @FormParam("newPass") String newPass,
                         @FormParam("recaptcha_response") String response) {

    log.info "Change password attempt received for user: $username"
    if (!verifyRecaptcha(response)) {
      return new Response(CAPTCHA_FAILED_ERROR)
    }
    
    try {
      
      log.trace "Attempting password change request..."
      FreeIPAClient client = mgr.resetPassword(username, password, newPass)
      log.info "User $username successfully changed password"
      
      RPCResponse<List<PwPortalUser>> r = (RPCResponse<List<PwPortalUser>>) client
          .userFind([], [uid: username])
      
      List<PwPortalUser> results = r.getResult() 
      if (results && results.size() > 0) {
        PwPortalUser user = results[0]
        String email = user.email
        if (email) {
          log.trace "Located user $username's email: $email"
          smtp.emailPasswordChangeNotification(
            email, username, new Date());
        }
      }
    
      log.trace "Returning success"
      return new Response()
      
    } catch(PasswordExpiredException e) {
      return new Response(PASSWORD_EXPIRED_ERROR)
      
    } catch(PasswordPolicyViolationException e) {
      return new Response(POLICY_VIOLATION_ERROR, e.localizedMessage)
      
    } catch(InvalidPasswordException | InvalidUserOrRealmException e) {
      return new Response(INVALID_CREDENTIALS_ERROR)
      
    } catch (Exception e) {
      log.error "Encountered exception while changing password", e
      return new Response(UNKNOWN_EXCEPTION_ERROR)
    }
  }
  
  /**
   * Assuming a valid user name and recaptcha response is supplied, issues 
   * a password reset email to the email matching the supplied user name
   * (according to the {@link SiteConfig#getPasswordResetEmailConfig()}.
   * Because this requires administrative access to the Kerberos realm, 
   * this endpoint will only behave as expected if the JAAS is properly
   * configured with a valid Kerberos and keytab configuration.
   * 
   * This endpoint logs all received reset requests and their subsequent 
   * success (if applicable) at INFO level.
   *  
   * @param user
   * @param response
   * @return
   */
  @Path("/resetrequest")
  @Produces("application/json")
  @POST
  Response resetRequest(@FormParam("user") String user,
                        @FormParam("recaptcha_response") String response) {
                 
   log.info "Received reset request for user: $user"
   if (!verifyRecaptcha(response)) {
     return new Response(CAPTCHA_FAILED_ERROR)
   }
   
   try {
     FreeIPAClient client = mgr.getKerberosClient()
     RPCResponse<List<PwPortalUser>> r = (RPCResponse<List<PwPortalUser>>) client
         .userFind([], [uid: user])
     
     List<PwPortalUser> results = r.result
     if (!results || results.size() < 1) {
       return new Response(INVALID_CREDENTIALS_ERROR)
     }
     
     PwPortalUser foundUser = results[0]
     String email = foundUser.email
     if (email == null) {
       return new Response(NO_EMAIL_ERROR)
     }
     
     Date now = new Date()
     TimeDuration timeLimit = 
       new TimeDuration(0, 0, config.passwordResetRequestTimeLimit, 0)
     Date expirationDate = TimeCategory.plus(now, timeLimit)
     
     def request = new PasswordResetRequest(
       requestId: generateRandomRid(),
       name: user,
       email: email,
       requestIp: getRemoteIp(),
       requestDate: now,
       expirationDate: expirationDate
     )
     
     def link = generateResetLink(request)
     cache.addRequest(request)
     
     log.debug "cache: $cache"
     
     smtp.emailPasswordResetUrl(email, request, link)
     log.info("Issued request link for $user that is valid "
            + "until $request.expirationDate")
     
     return new Response()
     
   } catch(e) {
     log.error "Encountered error while building password reset request", e
     return new Response(UNKNOWN_EXCEPTION_ERROR, e.message)
   }
  }
              
  /**
   * Fulfills a password reset request issued through the {@link 
   * #resetRequest(String, String)} endpoint. On successful password
   * change, an email is sent to the email matching the user name in
   * the Free IPA LDAP database for the passed request ID indicating 
   * the password was changed.
   * 
   * This endpoint logs all successful reset fulfillments at the INFO
   * level.
   * 
   * @param requestId
   * @param password
   * @return
   */
  @Path("/reset")
  @Produces("application/json")
  @POST
  Response reset(@FormParam("requestId") String requestId,
                 @FormParam("newPassword") String password) {
              
    PasswordResetRequest request = cache.getRequest(requestId)
    if (!request || request.expirationDate <= new Date()) {
      log.debug "Request expired: $requestId, cache: $cache"
      return new Response(INVALID_RESET_REQUEST_ID)
    }
    
    try {
      FreeIPAClient krbClient = mgr.getKerberosClient()
      def tmpPwd = requestId + password
      
      krbClient.passwd(request.name, tmpPwd)
      mgr.resetPassword(request.name, tmpPwd, password)
      log.info "Successfully reset password for user: $request.name"
      
      smtp.emailPasswordChangeNotification(
        request.email, request.name, new Date());
      cache.removeRequest(request)
      
      return new Response() 
    } catch(e) {
      log.error "Encountered error while resetting password", e
      return new Response(UNKNOWN_EXCEPTION_ERROR, e.message)
    }
    
  }
}
