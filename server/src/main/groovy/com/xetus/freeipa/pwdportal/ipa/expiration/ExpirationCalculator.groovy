package com.xetus.freeipa.pwdportal.ipa.expiration;

import com.xetus.freeipa.pwdportal.model.PwPortalUser

import groovy.transform.CompileStatic

import groovy.time.TimeCategory
import groovy.time.TimeDuration

@CompileStatic
public class ExpirationCalculator {
  public TimeDuration evaluate(PwPortalUser user) {
    if (!user.pwdExpiration) {
      return null;
    }
    return TimeCategory.minus(user.pwdExpiration, new Date());
  }
}
