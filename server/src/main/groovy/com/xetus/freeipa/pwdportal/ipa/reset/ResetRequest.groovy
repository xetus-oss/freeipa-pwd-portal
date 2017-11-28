package com.xetus.freeipa.pwdportal.ipa.reset

import groovy.transform.CompileStatic
import groovy.transform.ToString

@ToString
@CompileStatic
class ResetRequest {

  String resetId
  String token
  String name
  String email
  String requestIp
  Date requestDate
  Date expirationDate
  
  Map toBinding() {
    return [
      "resetId": this.resetId,
      "token": this.token,
      "name": this.name,
      "email": this.email,
      "requestIp": this.requestIp,
      "requestDate": this.requestDate,
      "expirationDate": this.expirationDate  
    ]
  }
}
