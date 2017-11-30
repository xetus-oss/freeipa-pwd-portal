package com.xetus.freeipa.pwdportal.api

import javax.servlet.http.HttpServletRequest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import com.xetus.freeipa.pwdportal.ipa.reset.ResetService
import com.xetus.freeipa.pwdportal.notification.EmailNotificationService
import com.xetus.freeipa.pwdportal.recaptcha.RecaptchaService
import com.xetus.oss.iris.PasswordPolicyViolationException
import com.xetus.oss.iris.ProbableLockOutException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import com.xetus.freeipa.pwdportal.ipa.reset.InvalidPasswordResetIdException
import com.xetus.freeipa.pwdportal.ipa.reset.ResetFulfillment
import com.xetus.freeipa.pwdportal.ipa.reset.ResetRequest

@Slf4j
@CompileStatic
@RestController
public class PasswordResetController {
  
  ResetService resetService
  RecaptchaService recaptcha
  
  @Autowired
  public PasswordResetController(ResetService resetService,
                                 RecaptchaService recaptcha) {
    this.resetService = resetService
    this.recaptcha = recaptcha
  }
  
  @RequestMapping(value = "/api/password/{user}/reset/request", method = RequestMethod.POST)
  public ResponseEntity<APIResponse> resetRequest(@PathVariable String user,
                                                  @RequestBody ResetRequestData data,
                                                  HttpServletRequest httpRequest) {
    log.info "Received reset request for user: $user"
    def requestIp = httpRequest.getRemoteAddr();
    if (!recaptcha.verifyRecaptcha(data.recaptchaResponse, requestIp)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(APIResponse.invalidRecaptcha())
    }

    try {
      def request = resetService.request(user, requestIp)
      return ResponseEntity.ok()
                           .body(new APIResponse("Successfully issued reset email"));
    } catch(IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                           .body(APIResponse.invalidCredentials());
    } catch(e) {
      log.error "Encountered error while building password reset request", e
      return ResponseEntity.status(500)
                           .body(APIResponse.serverError());
    }
  }
  
  @RequestMapping(value = "/api/password/{user}/reset", method = RequestMethod.POST)
  public ResponseEntity<APIResponse> reset(@PathVariable String user,
                                           @RequestBody ResetData data,
                                           HttpServletRequest httpRequest) {
    log.info "Received reset completion for user: $user"
    def requestIp = httpRequest.getRemoteAddr();
    if (!recaptcha.verifyRecaptcha(data.recaptchaResponse, requestIp)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
                           .body(APIResponse.invalidRecaptcha());
    }
    try {
      ResetFulfillment fulfillment = new ResetFulfillment(
        user: data.user,
        newPassword: data.password,
        resetId: data.resetId, 
        token: data.token 
      );
      ResetRequest request = resetService.reset(fulfillment)
      return ResponseEntity.ok()
                           .body(new APIResponse("Successfully reset password"));

    } catch(IllegalArgumentException e) {
      log.info "$user supplied invalid credentials"
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                           .body(APIResponse.invalidCredentials());

    } catch(InvalidPasswordResetIdException e) {
      log.info "$user supplied invalid reset id"
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                           .body(APIResponse.invalidResetId());
    
    } catch(ProbableLockOutException e) {
       log.info "$user is locked out or disabled"
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                           .body(APIResponse.lockedAccount());

    } catch(PasswordPolicyViolationException e) {
       log.info "new password for $user violated password policy"
      return ResponseEntity.badRequest()
                           .body(APIResponse.policyViolation(e.getViolations()));

    } catch(e) {
      log.error "Encountered error while resetting password", e
      return ResponseEntity.status(500).body(APIResponse.serverError());
    }
  }
  
  public static class ResetData {
    String resetId
    String token
    String password
    String user
    String recaptchaResponse
  }
  
  public static class ResetRequestData {
    String recaptchaResponse
  }
}
