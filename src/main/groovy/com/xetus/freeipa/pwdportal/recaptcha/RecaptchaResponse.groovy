package com.xetus.freeipa.pwdportal.recaptcha

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.transform.CompileStatic
import groovy.transform.ToString

@ToString(includeNames = true)
@CompileStatic
class RecaptchaResponse {

  Boolean success
  
  @JsonProperty("error-codes")
  List<String> errorCodes

  boolean isValid() {
    return success && (!errorCodes || errorCodes.size() < 1)
  }
  
}
