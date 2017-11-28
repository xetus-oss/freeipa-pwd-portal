package com.xetus.freeipa.pwdportal.recaptcha;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.github.mkopylec.recaptcha.validation.RecaptchaValidator
import com.github.mkopylec.recaptcha.validation.ValidationResult
import com.xetus.freeipa.pwdportal.PwdPortalConfig

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * A recaptcha shim to abstract the recaptcha library being used. This
 * is a good idea to facilitate migrating to a different library in the 
 * future 
 */
@Slf4j
@Component
@CompileStatic
public class RecaptchaService {
  
  PwdPortalConfig config;
  RecaptchaValidator validator
  
  @Autowired
  public RecaptchaService(PwdPortalConfig config, 
                          RecaptchaValidator validator) {
    this.config = config;
    this.validator = validator;
  }
  
  boolean verifyRecaptcha(String response, String requestIp) {
    if (config.disableRecaptcha) {
      return true
    }
    
    if (!response || response.empty) {
      log.warn "received empty, null, or falsey value for user " +
               "recaptcha response"
      return false
    }
    
    ValidationResult result = null
    try {
      result = validator.validate(response, requestIp)
    } catch(e) {
      log.error "Exception raised while consuming verification API", e
      return false
    }
        
    if (!result.isSuccess()) {
      log.warn "User failed recaptcha: ${result.getErrorCodes()}"
    }
    
    return result.isSuccess()
  }
}
