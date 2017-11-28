package com.xetus.freeipa.pwdportal.ipa.reset;

import java.security.SecureRandom
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.xetus.freeipa.pwdportal.PwdPortalConfig

import groovy.time.TimeCategory;
import groovy.time.TimeDuration;
import groovy.transform.CompileStatic

@Component
@CompileStatic
public class ResetRequestBuilder {
  
  private SecureRandom random
  private PwdPortalConfig config
  
  @Autowired
  public ResetRequestBuilder(PwdPortalConfig config) {
    this.config = config;
    this.random = new SecureRandom();
  }
  
  public ResetRequest build(String user, String email, String remoteIp) {
    Date now = new Date()
    TimeDuration timeLimit =
      new TimeDuration(0, 0, config.passwordResetRequestTimeLimit, 0)
    Date expirationDate = TimeCategory.plus(now, timeLimit)
     
    def request = new ResetRequest(
      resetId: generateRandom(),
      token: generateRandom(),
      name: user,
      email: email,
      requestIp: remoteIp,
      requestDate: now,
      expirationDate: expirationDate
    )
     
    return request;
  }
  
  /**
   * Utility method to generate random text.
   * @return the random request ID that was generated
   */
  private String generateRandom() {
    return new BigInteger(130, random).toString(32);
  }
  
}
