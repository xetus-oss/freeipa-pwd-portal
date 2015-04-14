package com.xetus.freeipa.pwdportal

import groovy.transform.CompileStatic
import groovy.transform.ToString

@ToString
@CompileStatic
class PasswordResetRequest {

  String requestId
  String name
  String email
  String requestIp
  Date requestDate
  Date expirationDate
  
  Map toBinding() {
    return [
      "requestId": this.requestId,
      "name": this.name,
      "email": this.email,
      "requestIp": this.requestIp,
      "requestDate": this.requestDate,
      "expirationDate": this.expirationDate  
    ]
  }
}
