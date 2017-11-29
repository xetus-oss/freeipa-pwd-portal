package com.xetus.freeipa.pwdportal.ipa.expiration;

import com.xetus.freeipa.pwdportal.model.PwPortalUser;

import groovy.time.TimeDuration;
import groovy.transform.CompileStatic;

@CompileStatic
public class ExpirationAuditResultEntry {
  private PwPortalUser user;
  private TimeDuration timeToExpiry;
  
  public ExpirationAuditResultEntry(PwPortalUser user, 
                                            TimeDuration timeToExpiry) {
    this.user = user;
    this.timeToExpiry = timeToExpiry;
  }
  
  public PwPortalUser getUser() {
    return this.user;
  }
  
  public TimeDuration getTimeToExpiry() {
    return this.timeToExpiry;
  }
}
